package common;

import common.io.assets.Admin.StaticPermitted;
import common.io.json.JsonClass;
import common.io.json.JsonClass.NoTag;
import common.io.json.JsonField;
import common.pack.Context;
import common.pack.Identifier;
import common.pack.SortedPackSet;
import common.pack.UserProfile;
import common.system.VImg;
import common.system.fake.FakeImage;
import common.util.Data;
import common.util.anim.ImgCut;
import common.util.anim.MaModel;
import common.util.pack.DemonSoul;
import common.util.pack.EffAnim.EffAnimStore;
import common.util.pack.NyCastle;
import common.util.stage.Music;
import common.util.unit.*;

import java.io.File;
import java.lang.Character;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Character.isDigit;

public class CommonStatic {

	public interface BattleConst {

		float ratio = 768f / 2400f;// r = p/u

	}

	public static class BCAuxAssets {

		// Res resources
		public final VImg[] slot = new VImg[3];
		public final VImg[][] ico = new VImg[2][];
		public final VImg[][] num = new VImg[9][11];
		public final VImg[][] battle = new VImg[3][];
		public final VImg[][] icon = new VImg[5][];
		public final VImg[] timer = new VImg[11];
		public final VImg[] spiritSummon = new VImg[4];

		public VImg emptyEdi = null;

		public Map<Integer, VImg> gatyaitem = new HashMap<>();
		public VImg XP;
		public VImg[][] moneySign = new VImg[4][4]; //Money on, off/Cost on, off
		/**
		 * Use this if trait.icon is null
		 */
		public VImg dummyTrait;
		public VImg waveShield;
		public VImg[] dmgIcons = new VImg[3];

		// Background resources
		public final List<ImgCut> iclist = new ArrayList<>();

		// Available data for orb, will be used for GUI
		// Map<Type, Map<Trait, Grades>>
		public final Map<Integer, Map<Integer, List<Integer>>> ORB = new TreeMap<>();
		public final Map<Integer, Integer> DATA = new HashMap<>();
		public final SortedPackSet<DemonSoul> demonSouls = new SortedPackSet<>();

		public FakeImage[] TYPES;
		public FakeImage[] TRAITS;
		public FakeImage[] GRADES;

		// NyCastle
		public final VImg[][] main = new VImg[3][NyCastle.TOT];
		public final NyCastle[] atks = new NyCastle[NyCastle.TOT];

		// EffAnim
		public final EffAnimStore effas = new EffAnimStore();
		public final int[][] values = new int[Data.C_TOT][5];
		public int[][] filter;

		// Form cuts
		public ImgCut unicut, udicut;

		// RandStage
		public final int[][] randRep = new int[5][];

		// def unit level
		public UnitLevel defLv;
	}

	@JsonClass(noTag = NoTag.LOAD)
	public static class LocalMaps {
		@JsonField(generic = { String.class, String.class })
		public HashMap<String, String> localLangMap = new HashMap<>();

		@JsonField(generic = { Integer.class, String.class })
		public HashMap<Integer, String> localMusicMap = new HashMap<>();
	}

	@JsonClass(noTag = NoTag.LOAD)
	public static class Config {

		// ImgCore
		public int deadOpa = 10, fullOpa = 90;
		public int[] ints = new int[] { 1, 1, 1, 2 };
		public boolean ref = true, battle = false, icon = false;
		public boolean twoRow = true;
		/**
		 * Use this variable to unlock plus level for aku outbreak
		 */
		public boolean plus = false;
		/**
		 * Use this variable to adjust level limit for aku outbreak
		 */
		public int levelLimit = 0;
		// Lang
		public int lang;
		/**
		 * Restoration target backup file, null means none
		 */
		public String backupFile;
		/**
		 * Used for partial restoration
		 */
		public String backupPath;
		/**
		 * Maximum number of backups, 0 means infinite
		 */
		public int maxBackup = 5;

		/**
		 * Preferred level for units
		 */
		public int prefLevel = 50;

		/**
		 * Decide whehter draw bg effect or not
		 */
		public boolean drawBGEffect = true;

		/**
		 * Enable 6f button delay on spawn
		 */
		public boolean buttonDelay = true;

		/**
		 * Color of background in viewer
		 */
		public int viewerColor = -1;

		/**
		 * Make BCU show ex stage continuation pop-up if true
		 */
		public boolean exContinuation = false;

		/**
		 * Make EX stage pop-up shown considering real chance
		 */
		public boolean realEx = false;

		/**
		 * Make stage name image displayed in battle
		 */
		public boolean stageName = true;

		/**
		 * Make battle shaken
		 */
		public boolean shake = true;

		/**
		 * Replace old music when updated
		 */
		public boolean updateOldMusic = true;

		/**
		 * Perform BC levelings
		 */
		public boolean realLevel = false;

		/**
		 * Use raw damage taken for TotalDamageTable
		 */
		public boolean rawDamage = true;

		/**
		 * Store whether to apply the combos from a given pack or not
		 */
		@JsonField(generic = { String.class, Boolean.class })
		public HashMap<String, Boolean> packCombos = new HashMap<>();

		/**
		 * Use progression mode to store save data
		 */
		public boolean prog = false;

		/**
		 * 60 fps mode
		 */
		public boolean fps60 = false;

		/**
		 * Stat
		 */
		public boolean stat = false;
	}

	@JsonClass
	public static class Faves {
		@JsonField(generic = AbForm.class, alias = AbForm.AbFormJson.class)
		public HashSet<AbForm> units = new HashSet<>();
		@JsonField(generic = AbEnemy.class, alias = Identifier.class)
		public SortedPackSet<AbEnemy> enemies = new SortedPackSet<>();
	}

	public interface EditLink {

		void review();

	}

	public interface FakeKey {

		boolean pressed(int i, int j);

		void remove(int i, int j);

	}

	public interface Itf {

		/**
		 * exit
		 */
		void save(boolean save, boolean genBackup, boolean exit);

		long getMusicLength(Music f);

		@Deprecated
		File route(String path);

		void setSE(int mus);

		void setSE(Identifier<Music> mus);

		void setBGM(Identifier<Music> mus);

		String getUILang(int m, String s);

		String[] lvText(AbForm f, Level lv);
	}

	public static class Lang {

		@StaticPermitted
		public static final String[] LOC_CODE = { "en", "zh", "kr", "jp", "ru", "de", "fr", "nl", "es", "it", "th" };

		@StaticPermitted
		public static final int[][] pref = {
				{ 0, 6, 9, 8, 5, 7, 4, 3, 1, 2 }, { 1, 3, 0, 2 }, { 2, 3, 0, 1 }, { 3, 0, 1, 2 },
				{ 4, 0, 3, 1, 2 }, { 5, 0, 4, 3, 1, 2 }, { 6, 9, 0, 7, 5, 4, 3, 1, 2 }, { 7, 0, 3, 1, 2, 4, 5, 6 },
				{ 8, 0, 6, 9, 5, 7, 4, 3, 1, 2 }, { 9, 0, 3, 1, 2, 5, 6, 7, 8, 4 }, { 10, 0, 3, 1, 2 }
		};

	}

	@StaticPermitted(StaticPermitted.Type.ENV)
	public static Itf def;

	@StaticPermitted(StaticPermitted.Type.ENV)
	public static Context ctx;

	@StaticPermitted(StaticPermitted.Type.FINAL)
	public static final BigInteger max = new BigInteger(String.valueOf(Integer.MAX_VALUE));
	@StaticPermitted(StaticPermitted.Type.FINAL)
	public static final BigDecimal maxdbl = new BigDecimal(String.valueOf(Double.MAX_VALUE));
	@StaticPermitted(StaticPermitted.Type.FINAL)
	public static final BigInteger min = new BigInteger(String.valueOf(Integer.MIN_VALUE));
	@StaticPermitted(StaticPermitted.Type.FINAL)
	public static final BigDecimal mindbl = new BigDecimal(String.valueOf(Double.MIN_VALUE));

	public static BCAuxAssets getBCAssets() {
		return UserProfile.getStatic("BCAuxAssets", BCAuxAssets::new);
	}

	public static LocalMaps getDataMaps() {
		return UserProfile.getStatic("localMaps", LocalMaps::new);
	}
	public static Config getConfig() {
		return UserProfile.getStatic("config", Config::new);
	}
	public static Faves getFaves() {
		return UserProfile.getStatic("faves", Faves::new);
	}

	public static boolean isInteger(String str) {
		str = str.trim();

		for (int i = 0; i < str.length(); i++)
			if (!Character.isDigit(str.charAt(i)))
				if(str.charAt(i) != '-' || i != 0)
					return false;
		return !str.isEmpty();
	}

	public static boolean isDouble(String str) {
		int dots = 0;
		for (int i = 0; i < str.length(); i++)
			if (!Character.isDigit(str.charAt(i))) {
				if((i == 0 && str.charAt(i) != '-') || str.charAt(i) != '.' || dots > 0)
					return false;
				else
					dots++;
			}
		return true;
	}

	public static int parseIntN(String str) {
		try {
			return parseIntsN(str)[0];
		} catch (Exception e) {
			return -1;
		}
	}

	public static String verifyFileName(String str) {
		return str.replaceAll("[\\\\/:*<>?\"|]", "_");
	}

	public static double parseDoubleN(String str) {
		try {
			return parseDoublesN(str)[0];
		} catch (Exception e) {
			return -1.0;
		}
	}

	public static float parseFloatN(String str) {
		try {
			return parseFloatsN(str)[0];
		} catch (Exception e) {
			return -1f;
		}
	}

	public static double[] parseDoublesN(String str) {
		ArrayList<String> lstr = new ArrayList<>();
		Matcher matcher = Pattern.compile("-?(([.|,]\\d+)|\\d+([.|,]\\d*)?)").matcher(str);

		while (matcher.find())
			lstr.add(matcher.group());

		double[] result = new double[lstr.size()];
		for (int i = 0; i < lstr.size(); i++)
			result[i] = safeParseDouble(lstr.get(i));
		return result;
	}

	public static float[] parseFloatsN(String str) {
		ArrayList<String> lstr = new ArrayList<>();
		Matcher matcher = Pattern.compile("-?(([.|,]\\d+)|\\d+([.|,]\\d*)?)").matcher(str);

		while (matcher.find())
			lstr.add(matcher.group());

		float[] result = new float[lstr.size()];

		for (int i = 0; i < lstr.size(); i++)
			result[i] = safeParseFloat(lstr.get(i));

		return result;
	}

	public static int[] parseIntsN(String str) {
		ArrayList<String> lstr = new ArrayList<>();
		int t = -1;
		for (int i = 0; i < str.length(); i++)
			if (t == -1) {
				if (isDigit(str.charAt(i)) || str.charAt(i) == '-')
					t = i;
			} else if (!isDigit(str.charAt(i))) {
				lstr.add(str.substring(t, i));
				t = -1;
			}
		if (t != -1)
			lstr.add(str.substring(t));
		int ind = 0;
		while (ind < lstr.size()) {
			if (isDigit(lstr.get(ind).charAt(0)) || lstr.get(ind).length() > 1)
				ind++;
			else
				lstr.remove(ind);
		}
		int[] ans = new int[lstr.size()];
		for (int i = 0; i < lstr.size(); i++)
			ans[i] = safeParseInt(lstr.get(i));
		return ans;
	}

	public static int safeParseInt(String v) {
		if(isInteger(v)) {
			BigInteger big = new BigInteger(v);

			if(big.compareTo(max) > 0) {
				return Integer.MAX_VALUE;
			} else if(big.compareTo(min) < 0) {
				return Integer.MIN_VALUE;
			} else {
				return Integer.parseInt(v);
			}
		} else {
			throw new IllegalStateException("Value "+v+" isn't a number");
		}
	}

	public static double safeParseDouble(String v) {
		if(isDouble(v)) {
			BigDecimal big = new BigDecimal(v);

			if(big.compareTo(maxdbl) > 0) {
				return Double.MAX_VALUE;
			} else if(big.compareTo(mindbl) < 0) {
				return Double.MIN_VALUE;
			} else {
				return Double.parseDouble(v);
			}
		} else {
			throw new IllegalStateException("Value "+v+" isn't a number");
		}
	}

	public static float safeParseFloat(String v) {
		if(isDouble(v)) {
			BigDecimal big = new BigDecimal(v);

			if(big.compareTo(maxdbl) > 0) {
				return Float.MAX_VALUE;
			} else if(big.compareTo(mindbl) < 0) {
				return Float.MIN_VALUE;
			} else {
				return Float.parseFloat(v);
			}
		} else {
			throw new IllegalStateException("Value "+v+" isn't a number");
		}
	}

	public static String[] getPackContentID(String input) {
		StringBuilder packID = new StringBuilder();
		StringBuilder entityID = new StringBuilder();

		boolean packEnd = false;

		for (int i = 0; i < input.length(); i++) {
			if (!packEnd) {
				if (Character.toString(input.charAt(i)).matches("[0-9a-z]"))
					packID.append(input.charAt(i));
				else {
					packEnd = true;
				}
			} else {
				if (Character.isDigit(input.charAt(i)))
					entityID.append(input.charAt(i));
			}
		}

		return new String[] { packID.toString(), entityID.toString() };
	}

	public static String[] getPackEntityID(String input) {
		String[] result = new String[2];

		StringBuilder packID = new StringBuilder();
		StringBuilder entityID = new StringBuilder();

		boolean packEnd = false;
		boolean findDigit = false;

		for(int i = 0; i < input.length(); i++) {
			if(!packEnd) {
				if(Character.toString(input.charAt(i)).matches("[0-9a-z]"))
					packID.append(input.charAt(i));
				else {
					packEnd = true;
					findDigit = true;
				}
			} else {
				if(findDigit) {
					if(Character.isDigit(input.charAt(i))) {
						entityID.append(input.charAt(i));
						findDigit = false;
					}
				} else {
					if(Character.isDigit(input.charAt(i))) {
						entityID.append(input.charAt(i));
					} else if(input.charAt(i) == 'r') {
						entityID.append(input.charAt(i));
						break;
					} else {
						break;
					}
				}
			}
		}

		result[0] = packID.toString();
		result[1] = entityID.toString();

		return result;
	}

	public static long parseLongN(String str) {
		long ans;
		try {
			ans = parseLongsN(str)[0];
		} catch (Exception e) {
			ans = -1;
		}
		return ans;
	}

	public static long[] parseLongsN(String str) {
		ArrayList<String> lstr = new ArrayList<>();
		int t = -1;
		for (int i = 0; i < str.length(); i++)
			if (t == -1) {
				if (isDigit(str.charAt(i)) || str.charAt(i) == '-' || str.charAt(i) == '+')
					t = i;
			} else if (!isDigit(str.charAt(i))) {
				lstr.add(str.substring(t, i));
				t = -1;
			}
		if (t != -1)
			lstr.add(str.substring(t));
		int ind = 0;
		while (ind < lstr.size()) {
			if (isDigit(lstr.get(ind).charAt(0)) || lstr.get(ind).length() > 1)
				ind++;
			else
				lstr.remove(ind);
		}
		long[] ans = new long[lstr.size()];
		for (int i = 0; i < lstr.size(); i++)
			ans[i] = Long.parseLong(lstr.get(i));
		return ans;
	}

	/**
	 * play sound effect
	 */
	public static void setSE(int ind) {
		def.setSE(ind);
	}

	/**
	 * play sound effect with identifier
	 */
	public static void setSE(Identifier<Music> mus) {
		def.setSE(mus);
	}

	/**
	 * play background music
	 * @param music Music
	 */
	public static void setBGM(Identifier<Music> music) {
		def.setBGM(music);
	}

	public static String toArrayFormat(int... data) {
		StringBuilder res = new StringBuilder("{");

		for (int i = 0; i < data.length; i++) {
			if (i == data.length - 1) {
				res.append(data[i]).append("}");
			} else {
				res.append(data[i]).append(", ");
			}
		}

		return res.toString();
	}

	/**
	 * Gets the minimum position value for a data enemy.
	 */
	public static float dataEnemyMinPos(MaModel model) {
		int y = model.confs[0][2];
		float z = (float) model.parts[0][8] / model.ints[0];
		return 2.5f * (float) Math.floor(y * z);
	}

	/**
	 * Gets the minimum position value for a custom enemy.
	 */
	public static float customEnemyMinPos(MaModel model) {
		int y = -model.parts[0][6];
		float z = (float) model.parts[0][8] / model.ints[0];
		return 2.5f * (float) Math.floor(y * z);
	}

	/**
	 * Gets the minimum position value for a data cat unit.
	 */
	public static int dataFormMinPos(MaModel model) {
		return (int) Math.max(0, 5 * Math.round((9.0 / 5.0) * model.confs[1][2] - 1));
	}

	/**
	 * Gets the minimum position value for a custom cat unit.
	 */
	public static int customFormMinPos(MaModel model) {
		return (int) Math.max(0, 5 * Math.round((9.0 / 5.0) * model.parts[0][6] - 1));
	}

	/**
	 * Gets the boss spawn point for a castle.
	 */
	public static float bossSpawnPoint(int y, int z) {
		return (3200 + (float) (y * z / 10) + Math.round(0.25f * Math.round(3.6f * z))) / 4f;
	}

	public static float fltFpsDiv(float f) {
		return getConfig().fps60 ? f / 2f : f;
	}

	public static float fltFpsMul(float f) {
		return getConfig().fps60 ? f * 2f : f;
	}
}
