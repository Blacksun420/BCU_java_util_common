package common.battle.attack;

import common.battle.data.MaskAtk;
import common.battle.entity.EEnemy;
import common.battle.entity.EUnit;
import common.battle.entity.EntCont;
import common.battle.entity.Entity;
import common.pack.Identifier;
import common.util.Data.Proc.SUMMON;
import common.util.unit.AbEnemy;
import common.util.unit.EForm;
import common.util.unit.UniRand;
import common.util.unit.Unit;
import org.jcodec.common.tools.MathUtil;

public class AtkModelEnemy extends AtkModelEntity {

	protected static final int cursedProcs = 14;

	protected AtkModelEnemy(EEnemy ent, double d0) {
		super(ent, d0, 1);
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

			if (proc.id == null || AbEnemy.class.isAssignableFrom(proc.id.cls)) {
				AbEnemy ene = Identifier.getOr(proc.id, AbEnemy.class);
				int allow = b.st.data.allow(b, ene);

				if (allow >= 0 || conf.ignore_limit) {
					double mula = proc.mult * 0.01;
					double mult = proc.mult * 0.01;

					if (!conf.fix_buff) {
						mult *= ((EEnemy) e).mult;
						mula *= ((EEnemy) e).mula;
					}

					mula *= (100.0 - resist) / 100;
					mult *= (100.0 - resist) / 100;
					for (int i = 0; i < proc.amount; i++) {
						int dis = proc.dis == proc.max_dis ? proc.dis : (int) (proc.dis + b.r.nextDouble() * (proc.max_dis - proc.dis + 1));
						double ep = ent.pos + getDire() * dis;
						EEnemy ee = ene.getEntity(b, acs, mult, mula, minlayer, maxlayer, 0);

						ee.group = allow;

						if (ep < ee.data.getWidth())
							ep = ee.data.getWidth();

						if (ep > b.st.len - 800)
							ep = b.st.len - 800;

						ee.added(1, (int) ep);

						b.tempe.add(new EntCont(ee, time));

						if (conf.same_health)
							ee.health = e.health;

						ee.setSummon(conf.anim_type, conf.bond_hp ? e : null);
					}
				}
			} else if (proc.id.cls == UniRand.class) {
				UniRand ur = Identifier.getOr(proc.id, UniRand.class);
				int lvl = (int) (proc.mult * ((EEnemy) e).mult);
				if (conf.fix_buff)
					lvl = proc.mult;
				for (int i = 0; i < proc.amount; i++) {
					int dis = proc.dis == proc.max_dis ? proc.dis : (int) (proc.dis + b.r.nextDouble() * (proc.max_dis - proc.dis + 1));
					double ep = ent.pos + getDire() * dis;
					EUnit eu = ur.getEntity(b, acs, b.max_num - b.entityCount(-1), minlayer, maxlayer, lvl, resist, null);
					if (eu != null) {
						if (conf.same_health)
							eu.health = e.health;

						eu.added(-1, (int) ep);
						b.tempe.add(new EntCont(eu, time));
						eu.setSummon(conf.anim_type, conf.bond_hp ? e : null);
					}
				}
			} else {
				Unit u = Identifier.getOr(proc.id, Unit.class);
				if (b.entityCount(-1) < b.max_num - u.forms[proc.form - 1].du.getWill() || conf.ignore_limit) {
					int dis = proc.dis == proc.max_dis ? proc.dis : (int) (proc.dis + b.r.nextDouble() * (proc.max_dis - proc.dis + 1));
					double ep = ent.pos + getDire() * dis;
					int lvl = (int) (proc.mult * ((EEnemy) e).mult);
					lvl = MathUtil.clip(lvl, 1, u.max + u.maxp);
					lvl *= (100.0 - resist) / 100;

					for (int i = 0; i < proc.amount; i++) {
						EForm ef = new EForm(u.forms[Math.max(proc.form - 1, 0)], lvl);
						EUnit eu = ef.invokeEntity(b, lvl, minlayer, maxlayer);
						if (conf.same_health)
							eu.health = e.health;

						eu.added(-1, (int) ep);
						b.tempe.add(new EntCont(eu, time));
						eu.setSummon(conf.anim_type, conf.bond_hp ? e : null);
					}
				}
			}
		} else
			ent.anim.getEff(INV);
	}

	@Override
	protected int getAttack(MaskAtk matk, Proc proc) {
		int atk = getEffAtk(matk);
		if (matk.getProc() != sealed && matk.canProc())
			setProc(matk, proc, e.status[P_CURSE][0] > 0 ? cursedProcs : 0);
		else {
			if (matk.getProc().MOVEWAVE.perform(b.r)) //Movewave procs regardless of seal state
				proc.MOVEWAVE.set(matk.getProc().MOVEWAVE);

			if (!matk.canProc())
				for (int j : BCShareable) proc.getArr(j).set(e.getProc().getArr(j));
		}

		extraAtk(matk);
		return atk;
	}
}
