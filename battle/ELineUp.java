package common.battle;

import common.CommonStatic;
import common.util.BattleObj;
import common.util.stage.Limit;

public class ELineUp extends BattleObj {

	public final int[][] price, cool, maxC;

	protected ELineUp(LineUp lu, StageBasis sb) {
		price = new int[2][5];
		cool = new int[2][5];
		maxC = new int[2][5];
		Limit lim = sb.est.lim;
		for (int i = 0; i < 2; i++)
			for (int j = 0; j < 5; j++) {
				if (lu.fs[i][j] == null) {
					price[i][j] = -1;
					continue;
				}
				price[i][j] = (int) (lu.efs[i][j].getPrice(sb.st.getCont().price) * 100);
				maxC[i][j] = sb.globalCdLimit() > 0
						? sb.b.t().getFinResGlobal(sb.globalCdLimit(), sb.isBanned(C_RESP))
						: sb.b.t().getFinRes(lu.efs[i][j].du.getRespawn(), sb.isBanned(C_RESP));
				if (lim != null && ((lim.line == 1 && i == 1) || lim.unusable(lu.efs[i][j].du, sb.st.getCont().price)))
					price[i][j] = -1;
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
