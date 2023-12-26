package common.battle.attack;

import common.battle.data.MaskAtk;
import common.battle.entity.EEnemy;

public class AtkModelEnemy extends AtkModelEntity {

	protected static final int cursedProcs = 16;

	protected AtkModelEnemy(EEnemy ent, float d0) {
		super(ent, d0, 1);
	}

	@Override
	protected int getAttack(MaskAtk matk, Proc proc) {
		int atk = getEffAtk(matk);
		if (matk.getProc() != empty && matk.canProc())
			setProc(matk, proc, e.status.curse > 0 ? cursedProcs : 1);
		else {
			if (matk.getProc().MOVEWAVE.perform(b.r)) //Movewave procs regardless of seal state
				proc.MOVEWAVE.set(matk.getProc().MOVEWAVE);

			if (!matk.canProc())
				for (int j : BCShareable) proc.getArr(j).set(e.getProc().getArr(j));
		}
		if (b.canon.deco > 0 && DECOS[b.canon.deco - 1] != -1) {
			Proc.PROB arr = (Proc.PROB)proc.getArr(DECOS[b.canon.deco - 1]);
			if (arr.prob > 0)
				if (arr instanceof Proc.PM)
					((Proc.PM) arr).mult *= b.b.t().getDecorationMagnification(b.canon.deco);
				else
					((Proc.PT) arr).time *= b.b.t().getDecorationMagnification(b.canon.deco);
		}

		extraAtk(matk);
		return atk;
	}
}
