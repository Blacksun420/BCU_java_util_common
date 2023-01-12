package common.battle.attack;

import common.battle.data.MaskAtk;
import common.battle.entity.EEnemy;

public class AtkModelEnemy extends AtkModelEntity {

	protected static final int cursedProcs = 16;

	protected AtkModelEnemy(EEnemy ent, double d0) {
		super(ent, d0, 1);
	}

	@Override
	protected int getAttack(MaskAtk matk, Proc proc) {
		int atk = getEffAtk(matk);
		if (matk.getProc() != sealed && matk.canProc())
			setProc(matk, proc, e.status.curse > 0 ? cursedProcs : 0);
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
