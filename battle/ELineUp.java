package common.battle;

import common.CommonStatic;
import common.util.BattleObj;
import common.util.stage.Limit;
import common.util.unit.Form;

public class ELineUp extends BattleObj {

	public final int[][] price, cool, maxC;

	protected ELineUp(LineUp lu, StageBasis sb) {
		price = new int[2][5];
		cool = new int[2][5];
		maxC = new int[2][5];
		Limit lim = sb.est.lim;
		for (int i = 0; i < 2; i++)
			for (int j = 0; j < 5; j++) {
				Form form = lu.fs[i][j];
				if (form == null) {
					price[i][j] = -1;
					continue;
				}
				if (lim != null && ((lim.line == 1 && i == 1) || lim.unusable(lu.efs[i][j].du, sb.st.getCont().price)))
					price[i][j] = -1;
				else
					price[i][j] = (int) (lu.efs[i][j].getPrice(sb.st.getCont().price) * 100);
				maxC[i][j] = sb.globalCdLimit() > 0
						? sb.b.t().getFinResGlobal(sb.globalCdLimit(), sb.isBanned(C_RESP))
						: sb.b.t().getFinRes(lu.efs[i][j].du.getRespawn(), sb.isBanned(C_RESP));
				if (lim != null && lim.stageLimit != null) {
					if (price[i][j] != -1)
						price[i][j] = price[i][j] * lim.stageLimit.costMultiplier[form.unit.rarity] / 100;
					maxC[i][j] = maxC[i][j] * lim.stageLimit.cooldownMultiplier[form.unit.rarity] / 100;
				}
			}
	}

	/**
	 * reset cooldown of a unit
	 */
	protected void get(int i, int j) {
		cool[i][j] = maxC[i][j];
	}

	/**
	 * count down the cooldown
	 */
	protected void update() {
		for (int i = 0; i < 2; i++)
			for (int j = 0; j < 5; j++) {
				if (cool[i][j] > 0) {
					cool[i][j]--;

					if (cool[i][j] == 0)
						CommonStatic.setSE(SE_SPEND_REF);
				}
			}
	}

}
