package common.battle;

import java.util.List;

import common.battle.data.PCoin;
import common.io.json.JsonClass;
import common.io.json.JsonField;
import common.io.json.JsonField.GenType;
import common.pack.Identifier;
import common.pack.UserProfile;
import common.system.Copable;
import common.util.BattleStatic;
import common.util.stage.Replay;
import common.util.unit.*;

import java.util.ArrayList;

@JsonClass
public class BasisLU extends Basis implements Copable<BasisLU>, BattleStatic {

	private static int[] getRandom(int n) {
		int[] ans = new int[n];
		int a = 0;
		for (int i = 0; i < n; i++) {
			int x = (int) (Math.random() * 10);
			while ((a & (1 << x)) > 0)
				x = (int) (Math.random() * 10);
			a |= 1 << x;
			ans[i] = x;
		}
		return ans;
	}

	public static List<BasisLU> allLus() {
		List<BasisLU> lus = new ArrayList<>();
		for (BasisSet set : BasisSet.list())
			lus.addAll(set.lb);
		for (Replay r : Replay.getMap().values())
			lus.add(r.lu);
		return lus;
	}

	private final Treasure t;

	@JsonField(gen = GenType.FILL)
	public final LineUp lu;

	@JsonField(gen = GenType.FILL)
	public int[] nyc = new int[3];

	public BasisLU() {
		t = new Treasure(this);
		lu = new LineUp();
	}

	public BasisLU(BasisSet bs) {
		t = new Treasure(this, bs.t());
		lu = new LineUp();
		name = "lineup " + bs.lb.size();
	}

	protected BasisLU(BasisSet bs, BasisLU bl) {
		t = new Treasure(this, bs.t());
		lu = new LineUp(bl.lu);
		name = "lineup " + bs.lb.size();
		nyc = bl.nyc.clone();
	}

	protected BasisLU(BasisSet bs, LineUp line, String str, int[] ints) {
		t = new Treasure(this, bs.t());
		name = str;
		lu = line;
		nyc = ints;
	}

	@Override
	public BasisLU copy() {
		return new BasisLU(BasisSet.current(), this);
	}

	@Override
	public int getInc(int type) {
		return lu.inc[type];
	}

	public BasisLU randomize(int n) {
		BasisLU ans = copy();
		int[] rad = getRandom(n);
		List<Unit> list = UserProfile.getBCData().units.getList();
		list.remove((Unit) Identifier.parseInt(339, Unit.class).get());
		for (AbForm[] fs : ans.lu.fs)
			for (AbForm f : fs)
				if (f instanceof Form)
					list.remove(((Form) f).unit);
		for (int i = 0; i < n; i++) {
			Unit u = list.get((int) (Math.random() * list.size()));
			list.remove(u);
			ans.lu.setFS(u.forms[u.forms.length - 1], rad[i]);
		}
		ans.lu.arrange();
		return ans;
	}

	public void simulateBCLeveling() {
		for(AbForm[] fs : lu.fs)
			for(int i = 0; i < fs.length; i++) {
				if(fs[i] == null || fs[i] instanceof UniRand)
					continue;
				Form f = (Form) fs[i];
				Level lv = lu.getLv(fs[i]);

				if(lv == null)
					throw new IllegalStateException("Battle started without initializing level of form in lineup");

				if (f.fid > 0 && lv.getTotalLv() < 10)
					fs[i] = f.unit.forms[0];
				else if (f.fid == 2)
					if (lv.getTotalLv() < f.unit.info.tfLevel && f.fid == 2)
						fs[i] = f.unit.forms[1];
					else if (f.fid == 2 && f.du.getPCoin() != null) {
						int[] talents = lv.getTalents();
						PCoin pc = f.du.getPCoin();
						for(int j = 0; j < Math.min(pc.info.size(), talents.length); j++)
							if(pc.getReqLv(j) > 0 && lv.getTotalLv() < pc.getReqLv(j))
								talents[j] = 0;

						int[][] orbs = lv.getOrbs();
						if(orbs != null && f.orbs != null && f.orbs.getSlots() != -1) {
							int[] limits = f.orbs.getLimits();
							for(int j = 0; j < orbs.length; j++)
								if(limits[j] == 1 && lv.getTotalLv() < 60)
									orbs[j] = new int[0];
						}
					}
			}
		lu.renew();
	}

	/**
	 * although the Treasure information is the same, this includes the effects of
	 * combo, so need to be an independent Treasure Object
	 */
	@Override
	public Treasure t() {
		return t;
	}

	/**
	 * Performs a similar operation to equals() function, but without overriding it. Checks if these 2 lineups are functionally identical
	 * @param blu The lineup to compare with
	 * @return True if they have the same castle parts, treasure levels, and units in lineup
	 */
	public boolean sameAs(BasisLU blu) {
		if (blu == null)
			return false;
		for (int i = 0; i < 3; i++)
			if (nyc[i] != blu.nyc[i])
				return false;
		return blu.t.equals(t) && blu.lu.equals(lu);
	}

}
