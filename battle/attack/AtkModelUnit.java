package common.battle.attack;

import common.battle.BasisLU;
import common.battle.data.MaskAtk;
import common.battle.data.PCoin;
import common.battle.entity.Entity;
import common.util.unit.Level;

public class AtkModelUnit extends AtkModelEntity {

	private final BasisLU bas;

	protected AtkModelUnit(Entity ent, double d0, double d1, PCoin pcoin, Level lv) {
		super(ent, d0, d1, pcoin, lv);
		bas = ent.basis.b;
	}

	@Override
	public int getEffAtk(MaskAtk matk) {
		int dmg = (int) (Math.round(matk.getAtk() * d1) * d0);
		if (e.status.weak[0] > 0)
			dmg = dmg * e.status.weak[1] / 100;
		if (e.status.strengthen != 0)
			dmg += dmg * (e.status.strengthen + bas.getInc(C_STRONG)) / 100;
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
			proc.getArr(P_BSTHUNT).set(e.getProc().getArr(P_BSTHUNT));
		} else {
			if (matk.getProc().MOVEWAVE.perform(b.r)) //Movewave procs regardless of seal state
				proc.MOVEWAVE.set(matk.getProc().MOVEWAVE);

			if (!matk.canProc()) {
				proc.getArr(P_BSTHUNT).set(e.getProc().getArr(P_BSTHUNT));
				for (int j : BCShareable) proc.getArr(j).set(e.getProc().getArr(j));
			}
		}
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
