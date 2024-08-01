package common.battle;

import common.CommonStatic;
import common.battle.entity.ESpirit;
import common.battle.entity.EUnit;
import common.pack.SortedPackSet;
import common.util.BattleObj;
import common.util.unit.Combo;
import common.util.unit.EForm;
import common.util.unit.Form;

public class ELineUp extends BattleObj {

	public final int[][] price = new int[2][5], cool = new int[2][5], maxC = new int[2][5];

	private final Proc.SPIRIT[][] spData = new Proc.SPIRIT[2][5];
	public final int[][] scd = new int[2][5], scount = new int[2][5], sGlow = new int[2][5];
	public final EUnit[][] smnd = new EUnit[2][5];

	public final int[] inc;

	protected ELineUp(LineUp lu, StageBasis sb, byte saveMode) {
		inc = lu.inc.clone();
		for (byte i = 0; i < inc.length; i++)
			if (sb.isBanned(i))
				inc[i] = 0;

		SortedPackSet<Combo> coms = new SortedPackSet<>(lu.coms);
        for (byte i = 0; i < 2; i++)
			for (byte j = 0; j < 5; j++) {
				if (lu.fs[i][j] == null)
					price[i][j] = -1;
				else if (saveMode == 2 && !sb.st.getMC().getSave(true).getUnlockedsBeforeStage(sb.st, true).containsKey(lu.fs[i][j]) ||
					saveMode == 1 && sb.st.getMC().getSave(true).locked(lu.fs[i][j]))
					price[i][j] = -2;
				else if (sb.st.lim != null && lu.efs[i][j] instanceof EForm && sb.st.lim.unusable(((EForm)lu.efs[i][j]).du, sb.st.getCont().price, i))
					price[i][j] = -1;
				if (price[i][j] != 0) {
					if (price[i][j] == -2)
						for (int k = 0; k < coms.size(); k++)
							if (inc[i] > 0 && coms.get(k).containsForm((Form)lu.fs[i][j])) {
								Combo c = coms.get(k); //1st check is so it doesn't become negative due to banned combo
								coms.remove(k--);
								inc[c.type] -= CommonStatic.getBCAssets().values[c.type][c.lv];
							}
					continue;
				}
				price[i][j] = (int) (lu.efs[i][j].getPrice(sb.st.getCont().price) * 100);
				maxC[i][j] = sb.globalCdLimit() > 0 ? sb.b.t().getFinResGlobal(sb.globalCdLimit(), getInc(C_RESP)) : sb.b.t().getFinRes(lu.efs[i][j].getRespawn(), getInc(C_RESP));

				spData[i][j] = lu.efs[i][j] instanceof EForm && ((EForm) lu.efs[i][j]).du.getProc().SPIRIT.exists() ? ((EForm)lu.efs[i][j]).du.getProc().SPIRIT : null;
				scount[i][j] = spData[i][j] == null ? -1 : 0;
			}
	}

	/**
	 * reset cooldown of a unit, as well as the values of a spirit
	 */
	protected void resetCD(int i, int j) {
		cool[i][j] = maxC[i][j];
		if (spData[i][j] != null) {
			scd[i][j] = spData[i][j].cd0;
			scount[i][j] = spData[i][j].amount;
		}
	}

	/**
	 * reset recharge time of a spirit and spawn it
	 */
	protected final void deploySpirit(int i, int j, StageBasis sb, EUnit spi) {//spi will always be an EUnit I just don't want to import it
		spi.added(-1, Math.min(Math.max(sb.ebase.pos + spi.data.getRange(), smnd[i][j].lastPosition + SPIRIT_SUMMON_RANGE), sb.ubase.pos));
		CommonStatic.setSE(SE_SPIRIT_SUMMON);
		sb.money -= spiritCost(i, j, sb.st.getCont().price);
		scount[i][j]--;
		scd[i][j] = spData[i][j].cd1;
		cool[i][j] = Math.min(Math.max(0, cool[i][j] + spData[i][j].summonerCd), maxC[i][j]);
		sb.le.add(spi);
		if (!(spi instanceof ESpirit))
			spi.setSummon(spData[i][j].animType, null);
	}

	public final boolean validSpirit(int i, int j) {
		return spData[i][j] != null && smnd[i][j] != null;
	}

	public final boolean readySpirit(int i, int j) {
		return validSpirit(i,j) && scd[i][j] == 0 && scount[i][j] > 0;
	}

	public final int spiritCost(int i, int j, int sta) {
		return (int)(spData[i][j].moneyCost * (1 + sta * 0.5f) * 100);
	}

	/**
	 * count down the cooldown
	 */
	protected void update(int time) {
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 5; j++) {
				if (cool[i][j] > 0 && --cool[i][j] == 0)
					CommonStatic.setSE(SE_SPEND_REF);

				if (validSpirit(i,j) && scount[i][j] > 0 && scd[i][j] > 0 && --scd[i][j] == 0)
					sGlow[i][j] = time;
			}
		}
	}

	/**
	 * Takes combo bans into account
	 * @param id combo ID
	 * @return buff of the specificed combo (0 if banned)
	 */
	public int getInc(int id) {
		return inc[id];
	}
}
