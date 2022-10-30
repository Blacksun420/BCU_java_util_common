package common.battle.attack;

import common.battle.BasisLU;
import common.battle.data.DataUnit;
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
	private final Proc[] buffed;

	protected AtkModelUnit(Entity ent, double d0, double d1, PCoin pcoin, Level lv) {
		super(ent, d0, d1, pcoin, lv);
		bas = ent.basis.b;
		buffed = new Proc[data.getAtkCount()];
		for (int i = 0; i < buffed.length; i++) {
			if(data.getAtkModel(i).getProc() == null)
				buffed[i] = Proc.blank();
			else
				buffed[i] = data.getAtkModel(i).getProc().clone();
			buffed[i].STOP.time = (buffed[i].STOP.time * (100 + bas.getInc(C_STOP))) / 100;
			buffed[i].SLOW.time = (buffed[i].SLOW.time * (100 + bas.getInc(C_SLOW))) / 100;
			buffed[i].WEAK.time = (buffed[i].WEAK.time * (100 + bas.getInc(C_WEAK))) / 100;
			if (buffed[i].CRIT.prob > 0)
				buffed[i].CRIT.prob += bas.getInc(C_CRIT);
		}
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
	protected int getAttack(int ind, Proc proc) {

		int atk = atks[ind];
		if (abis[ind] == 1) {
			setProc(ind, proc);
			proc.KB.dis = proc.KB.dis * (100 + bas.getInc(C_KB)) / 100;
		}
		if (e.data instanceof DataUnit)
			for (int j : BCShareable) proc.getArr(j).set(e.getProc().getArr(j));
		proc.getArr(P_BSTHUNT).set(e.getProc().getArr(P_BSTHUNT));

		extraAtk(ind);
		if (e.status[P_WEAK][0] > 0)
			atk = atk * e.status[P_WEAK][1] / 100;
		if (e.status[P_STRONG][0] != 0)
			atk += atk * (e.status[P_STRONG][0] + bas.getInc(C_STRONG)) / 100;
		atk *= e.auras.getAtkAura();
		return atk;
	}

	@Override
	public Proc getProc(int ind) {
		if (e.status[P_SEAL][0] > 0 || ind >= buffed.length)
			return super.getProc(ind);
		return buffed[ind];
	}

}
