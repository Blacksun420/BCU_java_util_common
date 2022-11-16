package common.battle.attack;

import common.battle.BasisLU;
import common.battle.data.MaskAtk;
import common.battle.data.PCoin;
import common.battle.entity.EEnemy;
import common.battle.entity.EUnit;
import common.battle.entity.EntCont;
import common.battle.entity.Entity;
import common.pack.Identifier;
import common.util.Data.Proc.SUMMON;
import common.util.unit.*;
import org.jcodec.common.tools.MathUtil;

public class AtkModelUnit extends AtkModelEntity {

	private final BasisLU bas;

	protected AtkModelUnit(Entity ent, double d0, double d1, PCoin pcoin, Level lv) {
		super(ent, d0, d1, pcoin, lv);
		bas = ent.basis.b;
	}

	@Override
	public void summon(SUMMON proc, Entity ent, Object acs, int resist) {
		if (resist < 100) {
			SUMMON.TYPE conf = proc.type;
			if (conf.same_health && ent.health <= 0)
				return;
			int time = proc.time;
			int minlayer = proc.min_layer, maxlayer = proc.max_layer;
			if (proc.min_layer == proc.max_layer && proc.min_layer == -1)
				minlayer = maxlayer = e.layer;

			if (proc.id == null || proc.id.cls == Unit.class) {
				Unit u = Identifier.getOr(proc.id, Unit.class);
				if (b.entityCount(-1) < b.max_num - u.forms[proc.form - 1].du.getWill() || conf.ignore_limit) {
					int lvl = proc.mult + ((EUnit) e).lvl;
					if (conf.fix_buff)
						lvl = proc.mult;
					lvl = MathUtil.clip(lvl, 1, u.max + u.maxp);
					lvl *= (100.0 - resist) / 100;

					for (int i = 0; i < proc.amount; i++) {
						int dis = proc.dis == proc.max_dis ? proc.dis : (int) (proc.dis + b.r.nextDouble() * (proc.max_dis - proc.dis + 1));
						double up = ent.pos + getDire() * dis;
						EForm ef = new EForm(u.forms[Math.max(proc.form - 1, 0)], proc.mult + ((EUnit) e).lvl);
						EUnit eu = ef.invokeEntity(b, lvl, minlayer, maxlayer);
						if (conf.same_health)
							eu.health = e.health;

						eu.added(-1, (int) up);
						b.tempe.add(new EntCont(eu, time));
						eu.setSummon(conf.anim_type, conf.bond_hp ? e : null);
					}
				}
			} else if (proc.id.cls == UniRand.class) {
				UniRand ur = Identifier.getOr(proc.id, UniRand.class);
				int lvl = proc.mult + ((EUnit) e).lvl;
				if (conf.fix_buff)
					lvl = proc.mult;
				for (int i = 0; i < proc.amount; i++) {
					int dis = proc.dis == proc.max_dis ? proc.dis : (int) (proc.dis + b.r.nextDouble() * (proc.max_dis - proc.dis + 1));
					double up = ent.pos + getDire() * dis;
					EUnit eu = ur.getEntity(b, acs, b.max_num - b.entityCount(-1), minlayer, maxlayer, lvl, resist, ((EUnit) e).index);
					if (eu != null) {
						if (conf.same_health)
							eu.health = e.health;

						eu.added(-1, (int) up);
						b.tempe.add(new EntCont(eu, time));
						eu.setSummon(conf.anim_type, conf.bond_hp ? e : null);
					}
				}
			} else {
				AbEnemy ene = Identifier.getOr(proc.id, AbEnemy.class);
				int allow = b.st.data.allow(b, ene);
				if (allow >= 0 || conf.ignore_limit) {
					double mula = proc.mult * 0.01;
					double mult = proc.mult * 0.01;
					if (!conf.fix_buff) {
						mula *= 1 + ((((EUnit) e).lvl - 1) * 0.2);
						mult *= 1 + ((((EUnit) e).lvl - 1) * 0.2);
					}

					mula *= (100.0 - resist) / 100;
					mult *= (100.0 - resist) / 100;
					for (int i = 0; i < proc.amount; i++) {
						int dis = proc.dis == proc.max_dis ? proc.dis : (int) (proc.dis + b.r.nextDouble() * (proc.max_dis - proc.dis + 1));
						double up = ent.pos + getDire() * dis;
						EEnemy ee = ene.getEntity(b, acs, mult, mula, minlayer, maxlayer, 0);

						ee.group = allow;
						if (up < ee.data.getWidth())
							up = ee.data.getWidth();
						if (up > b.st.len - 800)
							up = b.st.len - 800;

						ee.added(1, (int) up);
						b.tempe.add(new EntCont(ee, time));

						if (conf.same_health)
							ee.health = e.health;

						ee.setSummon(conf.anim_type, conf.bond_hp ? e : null);
					}
				}
			}
		} else
			ent.anim.getEff(INV);
	}

	@Override
	public int getEffAtk(MaskAtk matk) {
		int dmg = (int) (Math.round(matk.getAtk() * d0) * d1);
		if (e.status[P_WEAK][0] > 0)
			dmg = dmg * e.status[P_WEAK][1] / 100;
		if (e.status[P_STRONG][0] != 0)
			dmg += dmg * (e.status[P_STRONG][0] + bas.getInc(C_STRONG)) / 100;
		dmg *= e.auras.getAtkAura();

		return dmg;
	}

	@Override
	protected int getAttack(MaskAtk matk, Proc proc) {
		int atk = getEffAtk(matk);

		if (matk.getProc() != sealed && matk.canProc()) {
			setProc(matk, proc, 0);
			proc.KB.dis = proc.KB.dis * (100 + bas.getInc(C_KB)) / 100;
			proc.STOP.time = (proc.STOP.time * (100 + bas.getInc(C_STOP))) / 100;
			proc.SLOW.time = (proc.SLOW.time * (100 + bas.getInc(C_SLOW))) / 100;
			proc.WEAK.time = (proc.WEAK.time * (100 + bas.getInc(C_WEAK))) / 100;
		} else {
			if (matk.getProc().MOVEWAVE.perform(b.r)) //Movewave procs regardless of seal state
				proc.MOVEWAVE.set(matk.getProc().MOVEWAVE);

			if (!matk.canProc())
				for (int j : BCShareable) proc.getArr(j).set(e.getProc().getArr(j));
		}
		proc.getArr(P_BSTHUNT).set(e.getProc().getArr(P_BSTHUNT));

		extraAtk(matk);
		return atk;
	}

	@Override
	public Proc getProc(MaskAtk matk) {
		Proc p = super.getProc(matk);
		if (p.CRIT.prob == 0 || bas.getInc(C_CRIT) == 0)
			return p;
		Proc pp = p.clone();
		pp.CRIT.prob += bas.getInc(C_CRIT);
		return pp;
	}
}
