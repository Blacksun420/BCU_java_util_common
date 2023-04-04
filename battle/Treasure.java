package common.battle;

import com.google.common.primitives.Ints;
import common.CommonStatic;
import common.battle.data.Orb;
import common.io.json.JsonClass;
import common.io.json.JsonClass.RType;
import common.io.json.JsonField;
import common.io.json.JsonField.GenType;
import common.pack.FixIndexList.FixIndexMap;
import common.pack.SortedPackSet;
import common.pack.UserProfile;
import common.system.files.VFile;
import common.util.Data;
import common.util.unit.Level;
import common.util.unit.Trait;

import java.util.*;

@SuppressWarnings("ForLoopReplaceableByForEach")
@JsonClass(read = RType.FILL)
public class Treasure extends Data {
	public static void readCannonCurveData() {
		String[] targetFiles = {
				"CC_AllParts_growth.csv",
				"CC_BaseParts_growth.csv",
				"CC_DecoParts_growth.csv"
		};

		for(int i = 0; i < targetFiles.length; i++) {
			VFile vf = VFile.get("./org/data/" + targetFiles[i]);

			if(vf != null) {
				Map<Integer, CannonLevelCurve> target;
				CannonLevelCurve.PART part;

				switch (i) {
					case 0:
						target = curveData;
						part = CannonLevelCurve.PART.CANNON;

						break;
					case 1:
						target = baseData;
						part = CannonLevelCurve.PART.BASE;

						break;
					default:
						target = decorationData;
						part = CannonLevelCurve.PART.DECORATION;
				}

				injectData(vf, target, part);
			} else {
				System.out.println("W/Treasure::readCannonCurveData - No such file called ./org/data/" + targetFiles[i] + " in VFile");
			}
		}
	}

	private static void injectData(VFile vf, Map<Integer, CannonLevelCurve> target, CannonLevelCurve.PART part) {
		Queue<String> q = vf.getData().readLine();

		q.poll();

		Map<Integer, Map<Integer, ArrayList<ArrayList<Integer>>>> initCurve = new HashMap<>();
		Map<Integer, Integer> maxLevels = new HashMap<>();
		Map<Integer, Integer> previousMaxLevel = new HashMap<>();

		int previousType = -1;

		String line;

		while((line = q.poll()) != null) {
			int[] data = CommonStatic.parseIntsN(line);

			int id = data[0];

			//Skip analyzing data about normal cannon
			if(id == 0)
				continue;

			Map<Integer, ArrayList<ArrayList<Integer>>> curveData;

			if(initCurve.containsKey(id)) {
				curveData = initCurve.get(id);
			} else {
				curveData = new HashMap<>();
			}

			int type = data[1];

			if(type != previousType)
				previousMaxLevel.clear();

			ArrayList<ArrayList<Integer>> curves;

			if(curveData.containsKey(type)) {
				curves = curveData.get(type);
			} else {
				curves = new ArrayList<>();

				curves.add(new ArrayList<>());
				curves.add(new ArrayList<>());
			}

			int maxLevel = data[2];

			if(!maxLevels.containsKey(id) || maxLevels.get(id) < maxLevel) {
				maxLevels.put(id, maxLevel);
			}

			int difference;

			if(previousMaxLevel.containsKey(id)) {
				difference = maxLevel - previousMaxLevel.get(id);
			} else {
				difference = maxLevel;
			}

			int min = data[3];
			int max = data[4];

			double segment = (max - min) * 1.0 / (difference / 10.0);

			int mn;
			int mx;

			for(int i = 0; i < difference; i += 10) {
				mn = min + (int) (segment * i / 10);
				mx = min + (int) (segment * (i / 10 + 1));

				curves.get(0).add(mn);
				curves.get(1).add(mx);
			}

			curveData.put(type, curves);

			initCurve.put(id, curveData);

			previousMaxLevel.put(id, maxLevel);
			previousType = type;
		}

		for(int id : initCurve.keySet()) {
			Map<Integer, ArrayList<ArrayList<Integer>>> curveData = initCurve.get(id);

			Map<Integer, int[][]> filteredData = new HashMap<>();

			for(int type : curveData.keySet()) {
				ArrayList<ArrayList<Integer>> curves = curveData.get(type);

				int[][] filteredCurves = new int[2][];

				filteredCurves[0] = Ints.toArray(curves.get(0));
				filteredCurves[1] = Ints.toArray(curves.get(1));

				filteredData.put(type, filteredCurves);
			}

			target.put(id, new CannonLevelCurve(filteredData, maxLevels.get(id), part));
		}
	}

	public static final Map<Integer, CannonLevelCurve> curveData = new HashMap<>();
	public static final Map<Integer, CannonLevelCurve> baseData = new HashMap<>();
	public static final Map<Integer, CannonLevelCurve> decorationData = new HashMap<>();

	public final Basis b;

	@JsonField(gen = GenType.FILL)
	public int[] tech = new int[LV_TOT],
			trea = new int[T_TOT],
			bslv = new int[BASE_TOT],
			base = new int[DECO_BASE_TOT],
			deco = new int[DECO_BASE_TOT],
			fruit = new int[7],
			gods = new int[3];

	@JsonField
	public int alien, star;

	/**
	 * new Treasure object
	 */
	protected Treasure(Basis bas) {
		b = bas;
		zread$000000();
	}

	/**
	 * copy Treasure object
	 */
	protected Treasure(Basis bas, Treasure t) {
		b = bas;
		tech = t.tech.clone();
		trea = t.trea.clone();
		fruit = t.fruit.clone();
		gods = t.gods.clone();
		alien = t.alien;
		star = t.star;
		bslv = t.bslv.clone();
		base = t.base.clone();
		deco = t.deco.clone();
	}

	/**
	 * get multiplication of non-starred alien
	 */
	public double getAlienMulti() {
		return 7 - alien * 0.01;
	}

	/**
	 * get cat attack multiplication
	 */
	public double getAtkMulti() {
		return 1 + trea[T_ATK] * 0.005;
	}

	/**
	 * get base health
	 */
	public int getBaseHealth() {
		int t = tech[LV_BASE];
		int base = t < 6 ? t * 1000 : t < 8 ? 5000 + (t - 5) * 2000 : 9000 + (t - 7) * 3000;
		base += trea[T_BASE] * 70;
		if (bslv[0] > 10)
			base += 36000 + 4000 * (bslv[0] - 10);
		else
			base += 3600 * bslv[0];
		return base * (100 + b.getInc(C_BASE)) / 100;
	}

	/**
	 * get normal canon attack
	 */
	public int getCanonAtk() {
		int base = 50 + tech[LV_CATK] * 50 + trea[T_CATK] * 5;
		return base * (100 + b.getInc(C_C_ATK)) / 100;
	}

	public double getCannonMagnification(int id, int type) {
		if(curveData.containsKey(id)) {
			CannonLevelCurve levelCurve = curveData.get(id);

			return levelCurve.applyFormula(type, bslv[id]);
		}

		System.out.println("Warning : Unknown ID : "+ id);

		return 0;
	}

	public double getBaseMagnification(int id, SortedPackSet<Trait> traits) {
		double ans = 1.0;
		FixIndexMap<Trait> BCTraits = UserProfile.getBCData().traits;

		byte trait;
		byte traitData;
		switch (id) {
			case DECO_BASE_SLOW:
				trait = traitData = TRAIT_FLOAT; //traitData = BASE_FLOAT;
				break;
			case DECO_BASE_WALL:
				trait = traitData = TRAIT_BLACK;
				break;
			case DECO_BASE_STOP:
				trait = traitData = TRAIT_ANGEL;
				break;
			case DECO_BASE_WATER:
				trait = traitData = TRAIT_RED;
				break;
			case DECO_BASE_GROUND:
				trait = traitData = TRAIT_ZOMBIE;
				break;
			case DECO_BASE_BARRIER:
				trait = traitData = TRAIT_ALIEN;
				break;
			case DECO_BASE_CURSE:
				trait = TRAIT_RELIC;
				traitData = TRAIT_RELIC - 1;//Aku exists
				break;
			default:
				return ans;
		}
		if(traits.contains(BCTraits.get(trait))) {
			CannonLevelCurve clc = baseData.get(id);
			if(clc == null)
				return ans;
			if(base[id - 1] == 0)
				return ans;
			ans = clc.applyFormula(traitData, base[id - 1]);
		}
		return ans;
	}

	public double getDecorationMagnification(int id) {
		if(deco[id - 1] == 0)
			return 1.0;
		CannonLevelCurve clc = decorationData.get(id);

		if(clc == null)
			return 1.0;
		return clc.applyFormula(id - 1, deco[id - 1]);
	}

	/**
	 * get cat health multiplication
	 */
	public double getDefMulti() {
		return 1 + trea[T_DEF] * 0.005;
	}

	/**
	 * get accounting multiplication
	 */
	public double getDropMulti() {
		return (0.95 + 0.05 * tech[LV_ACC] + 0.005 * trea[T_ACC]) * (1 + b.getInc(C_MEAR) * 0.01);
	}

	/**
	 * get EVA kill ability attack multiplication
	 */
	public double getEKAtk() {
		return 0.05 * (100 + b.getInc(C_EKILL));
	}

	/**
	 * get EVA kill ability reduce damage multiplication
	 */
	public double getEKDef() {
		return 20.0 / (100 + b.getInc(C_EKILL));
	}

	/**
	 * get processed cat cool down time
	 * max treasure & level should lead to -264f recharge
	 */
	public int getFinRes(int ori) {
		double research = (tech[LV_RES] - 1) * 6 + trea[T_RES] * 0.3;
		double deduction = research + Math.floor(research * b.getInc(C_RESP) / 100);
		return (int) Math.max(60, ori - deduction);
	}

	/**
	 * get reverse cat cool down time
	 */
	public int getRevRes(int res) {
		double research = (tech[LV_RES] - 1) * 6 + trea[T_RES] * 0.3;
		double addition = research + Math.floor(research * b.getInc(C_RESP) / 100);
		return (int) Math.max(60, res + addition);

	}

	/**
	 * get maximum fruit of certain trait bitmask
	 */
	public double getFruit(SortedPackSet<Trait> types) {
		double ans = 0;
		FixIndexMap<Trait> BCTraits = UserProfile.getBCData().traits;
		if (types.contains(BCTraits.get(Data.TRAIT_RED)))
			ans = Math.max(ans, fruit[T_RED]);
		if (types.contains(BCTraits.get(Data.TRAIT_FLOAT)))
			ans = Math.max(ans, fruit[T_FLOAT]);
		if (types.contains(BCTraits.get(Data.TRAIT_BLACK)))
			ans = Math.max(ans, fruit[T_BLACK]);
		if (types.contains(BCTraits.get(Data.TRAIT_METAL)))
			ans = Math.max(ans, fruit[T_METAL]);
		if (types.contains(BCTraits.get(Data.TRAIT_ANGEL)))
			ans = Math.max(ans, fruit[T_ANGEL]);
		if (types.contains(BCTraits.get(Data.TRAIT_ALIEN)))
			ans = Math.max(ans, fruit[T_ALIEN]);
		if (types.contains(BCTraits.get(Data.TRAIT_ZOMBIE)))
			ans = Math.max(ans, fruit[T_ZOMBIE]);
		return ans * 0.01;
	}

	/**
	 * get damage reduce multiplication from strong against ability
	 */
	public double getGOODDEF(SortedPackSet<Trait> eTraits, SortedPackSet<Trait> traits, Orb orb, Level level) {
		double ini = traits.isEmpty() ? 1 : 0.5 - 0.1 / 3 * getFruit(traits);

		if(orb != null && level.getOrbs() != null) {
			int[][] orbs = level.getOrbs();

			for(int i = 0; i < orbs.length; i++) {
				if (orbs[i].length < ORB_TOT)
					continue;

				if (orbs[i][ORB_TYPE] == ORB_STRONG) {
					List<Trait> orbType = Trait.convertOrb(orbs[i][ORB_TRAIT]);

					for(int j = 0; j < orbType.size(); j++) {
						if(eTraits.contains(orbType.get(j))) {
							ini *= 1 - ORB_STR_DEF_MULTI[orbs[i][ORB_GRADE]] / 100.0;

							break;
						}
					}
				}
			}
		}

		if (ini == 1)
			return ini;

		double com = 1 - b.getInc(C_GOOD) * 0.01;

		return ini * com;
	}

	/**
	 * get attack multiplication from super massive damage ability
	 */
	public double getMASSIVESATK(SortedPackSet<Trait> traits) {
		return 5 + 1.0 / 3 * getFruit(traits);
	}

	/**
	 * get attack multiplication from massive damage ability
	 */
	public double getMASSIVEATK(SortedPackSet<Trait> traits) {
		double ini = 3 + 1.0 / 3 * getFruit(traits);
		double combo = (1 - (b.getInc(C_MASSIVE) * 0.01));
		return ini * combo;
	}

	/**
	 * get attack multiplication from massive damage ability
	 */
	public double getGOODATK(SortedPackSet<Trait> traits) {
		double ini = 1.5 + 0.3 / 3 * getFruit(traits);
		double combo = 1 - (b.getInc(C_GOOD) * 0.01);
		return ini * combo;
	}

	/**
	 * get damage reduce multiplication from resistant ability
	 */
	public double getRESISTDEF(SortedPackSet<Trait> eTraits, SortedPackSet<Trait> traits, Orb orb, Level level) {
		double ini = traits.isEmpty() ? 1 : 0.25 - 0.05 / 3 * getFruit(traits);

		if(orb != null && level.getOrbs() != null) {
			int[][] orbs = level.getOrbs();

			for(int i = 0; i < orbs.length; i++) {
				if (orbs[i].length < ORB_TOT)
					continue;

				if (orbs[i][ORB_TYPE] == ORB_RESISTANT) {
					List<Trait> orbType = Trait.convertOrb(orbs[i][ORB_TRAIT]);

					for(int j = 0; j < orbType.size(); j++) {
						if(eTraits.contains(orbType.get(j))) {
							ini *= 1 - ORB_RESISTANT_MULTI[orbs[i][ORB_GRADE]] / 100.0;

							break;
						}
					}
				}
			}
		}

		if (ini == 1)
			return ini;

		double com = 1 - b.getInc(C_RESIST) * 0.01;
		return ini * com;
	}

	/**
	 * get damage reduce multiplication from super resistant ability
	 */
	public double getRESISTSDEF(SortedPackSet<Trait> traits) {
		return 1.0 / 6 - 1.0 / 126 * getFruit(traits);
	}

	/**
	 * get multiplication of starred enemy
	 */
	public double getStarMulti(int st) {
		if (st == 1)
			return 16 - star * 0.01;
		else
			return 11 - 0.1 * gods[st - 2];
	}

	/**
	 * get witch kill ability attack multiplication
	 */
	public double getWKAtk() {
		return 0.05 * (100 + b.getInc(C_WKILL));
	}

	/**
	 * get witch kill ability reduce damage multiplication
	 */
	public double getWKDef() {
		return 10.0 / (100 + b.getInc(C_WKILL));
	}

	public double getXPMult() {
		int txp1 = trea[T_XP1];
		int txp2 = trea[T_XP2];
		double tm = txp1 * 0.005 + txp2 * 0.0025;
		return 0.95 + tech[LV_XP] * 0.05 + tm;
	}

	/**
	 * get canon recharge time
	 */
	protected int CanonTime(int map) {
		int base = 1500 + 50 * (tech[LV_CATK] - tech[LV_RECH]);
		if (trea[T_RECH] <= 300)
			base -= (int) (1.5 * trea[T_RECH]);
		else
			base -= 3 * trea[T_RECH] - 450;
		base -= b.getInc(C_C_SPE);
		base = Math.max(950, base + map * 450);
		return base;
	}

	/**
	 * get the cost to upgrade worker cat
	 */
	protected int getLvCost(int lv) {
		int t = tech[LV_WORK];
		int base = t < 8 ? 30 + 10 * t : 20 * t - 40;
		return lv >= 8 ? -1 : base * lv * 100;
	}

	/**
	 * get wallet capacity
	 */
	protected int getMaxMon(int lv) {
		int base = Math.max(25, 50 * tech[LV_WALT]);
		base = base * (1 + lv);
		base += trea[T_WALT] * 10;
		return base * (100 + b.getInc(C_M_MAX));
	}

	/**
	 * get money increase rate
	 */
	protected int getMonInc(int lv) {
		double output = (0.15 + 0.1 * tech[LV_WORK]) * (1 + (lv - 1) * 0.1) + trea[T_WORK] * 0.01;
		return (int) Math.round(output * 100);
	}

	private void zread$000000() {
		System.arraycopy(MLV, 0, tech, 0, LV_TOT);
		System.arraycopy(MT, 0, trea, 0, T_TOT);
		fruit[T_RED] = fruit[T_BLACK] = fruit[T_FLOAT] = fruit[T_ANGEL] = 300;
		fruit[T_METAL] = fruit[T_ZOMBIE] = fruit[T_ALIEN] = 300;
		bslv[0] = 30;
		for (int i = 1; i < BASE_TOT; i++) {
			bslv[i] = curveData.get(i).max;
			base[i - 1] = baseData.get(i).max;
			deco[i - 1] = decorationData.get(i).max;
		}
		gods[0] = gods[1] = gods[2] = 100;
		alien = 600;
		star = 1500;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof Treasure))
			return false;
		Treasure tres = (Treasure) obj;
		if (alien != tres.alien || star != tres.star)
			return false;
		return Arrays.equals(gods, tres.gods) && Arrays.equals(fruit, tres.fruit) && Arrays.equals(bslv, tres.bslv)
				&& Arrays.equals(tech, tres.tech) && Arrays.equals(trea, tres.trea);
	}
}
