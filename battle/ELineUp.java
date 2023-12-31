package common.battle;

import common.CommonStatic;
import common.battle.entity.ESpirit;
import common.battle.entity.Entity;
import common.pack.SortedPackSet;
import common.util.BattleObj;
import common.util.stage.Limit;
import common.util.unit.Combo;
import common.util.unit.EForm;
import common.util.unit.Form;

public class ELineUp extends BattleObj {

	public final int[][] price, cool, maxC;

	Proc.SPIRIT[][] spData = new Proc.SPIRIT[2][5];
	public final int[][] scd, scount, slayer;
	public final float[][] spos = new float[2][5];

	public final int[] inc;

	protected ELineUp(LineUp lu, StageBasis sb, boolean sav) {
		price = new int[2][5];
		cool = new int[2][5];
		maxC = new int[2][5];

		scd = new int[2][5];
		scount = new int[2][5];
		slayer = new int[2][5];
		inc = lu.inc.clone();
		SortedPackSet<Combo> coms = new SortedPackSet<>(lu.coms);
		Limit lim = sb.est.lim;
		for (int i = 0; i < 2; i++)
			for (int j = 0; j < 5; j++) {
				if (lu.fs[i][j] == null ||
						(lim != null && ((lim.line > 0 && 2 - (lim.line - i) != 1) || (lu.efs[i][j] instanceof EForm && lim.unusable(((EForm) lu.efs[i][j]).du, sb.st.getCont().price))))
						|| (sav && sb.st.getCont().getCont().getSave(true).locked(lu.fs[i][j]))) {
					price[i][j] = -1;
					if (sav && lu.fs[i][j] instanceof Form && sb.st.getCont().getCont().getSave(true).locked(lu.fs[i][j]))
						for (int k = 0; k < coms.size(); k++)
							if (coms.get(k).containsForm((Form)lu.fs[i][j])) {
								Combo c = coms.get(k);
								coms.remove(k--);
								inc[c.type] -= CommonStatic.getBCAssets().values[c.type][c.lv];
							}
					continue;
				}
				price[i][j] = (int) (lu.efs[i][j].getPrice(sb.st.getCont().price) * 100);
				maxC[i][j] = sb.b.t().getFinRes(lu.efs[i][j].getRespawn());

				spData[i][j] = lu.fs[i][j] instanceof Form && ((Form) lu.fs[i][j]).du.getProc().SPIRIT.exists() ? ((Form) lu.fs[i][j]).du.getProc().SPIRIT : null;
				scount[i][j] = spData[i][j] == null ? -1 : 0;
			}
	}

	/**
	 * reset recharge time of a unit, as well as the values of a spirit
	 */
	protected void resetCD(int i, int j) {
		cool[i][j] = maxC[i][j];
		scd[i][j] = spData[i][j].cd0;
		scount[i][j] = spData[i][j].amount;
	}

	/**
	 * reset recharge time of a spirit and spawn it
	 */
	protected final void deploySpirit(int i, int j, StageBasis sb, Entity spi) {//spi will always be an EUnit I just don't want to import it
		spi.added(-1, Math.min(Math.max(sb.ebase.pos + spi.data.getRange(), spos[i][j] + 150), sb.st.len - 700));
		CommonStatic.setSE(SE_SPEND_SUC); //money -= ((MaskUnit)spirit.data).getPrice() * (1 + st.getCont().price * 50);
		scount[i][j]--;
		scd[i][j] = spData[i][j].cd1;
		cool[i][j] = Math.min(Math.max(0, cool[i][j] + spData[i][j].summonerCd), maxC[i][j]);
		sb.le.add(spi);
		if (!(spi instanceof ESpirit))
			spi.setSummon(spData[i][j].animType, null);
	}

	public final boolean validSpirit(int i, int j) {
		return spData[i][j] != null && spos[i][j] < Float.MAX_VALUE;
	}

	public final boolean readySpirit(int i, int j) {
		return validSpirit(i,j) && scd[i][j] == 0 && scount[i][j] > 0;
	}

	/**
	 * count down the cool down time
	 */
	protected void update() {
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 5; j++) {
				if (cool[i][j] > 0 && --cool[i][j] == 0)
					CommonStatic.setSE(SE_SPEND_REF);

				if (validSpirit(i,j) && scd[i][j] > 0 && scount[i][j] > 0 && --scd[i][j] == 0)
					CommonStatic.setSE(SE_SPIRIT_SUMMON);
				spos[i][j] = Float.MAX_VALUE;
			}
		}
	}

	public int getInc(int type) {
		return inc[type];
	}
}
