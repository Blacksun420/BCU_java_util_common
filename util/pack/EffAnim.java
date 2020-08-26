package common.util.pack;

import common.CommonStatic;
import common.system.VImg;
import common.system.fake.FakeImage;
import common.system.fake.FakeImage.Marker;
import common.util.Data;
import common.util.anim.*;

import java.lang.reflect.Field;
import java.util.function.Function;

public class EffAnim<T extends Enum<T> & EffAnim.EffType<T>> extends AnimD<EffAnim<T>, T> {

	public enum ArmorEff implements EffType<ArmorEff> {
		BUFF("buff"), DEBUFF("debuff");

		private final String path;

		ArmorEff(String str) {
			path = str;
		}

		@Override
		public String path() {
			return path;
		}
	}

	public enum BarEneEff implements EffType<BarEneEff> {
		BREAK("_breaker"), DESTR("_destruction");

		private final String path;

		BarEneEff(String str) {
			path = str;
		}

		@Override
		public String path() {
			return path;
		}
	}

	public enum BarrierEff implements EffType<BarrierEff> {
		BREAK("_breaker"), DESTR("_destruction"), DURING("_during"), START("_start"), END("_end");

		private final String path;

		BarrierEff(String str) {
			path = str;
		}

		@Override
		public String path() {
			return path;
		}
	}

	public enum DefEff implements EffType<DefEff> {
		DEF("");

		private final String path;

		DefEff(String str) {
			path = str;
		}

		@Override
		public String path() {
			return path;
		}

	}

	public static class EffAnimStore {

		public EffAnim<DefEff> A_DOWN;
		public EffAnim<DefEff> A_E_DOWN;
		public EffAnim<DefEff> A_UP;
		public EffAnim<DefEff> A_E_UP;
		public EffAnim<DefEff> A_SLOW;
		public EffAnim<DefEff> A_E_SLOW;
		public EffAnim<DefEff> A_STOP;
		public EffAnim<DefEff> A_E_STOP;
		public EffAnim<DefEff> A_SHIELD;
		public EffAnim<DefEff> A_E_SHIELD;
		public EffAnim<DefEff> A_FARATTACK;
		public EffAnim<DefEff> A_E_FARATTACK;
		public EffAnim<DefEff> A_WAVE_INVALID;
		public EffAnim<DefEff> A_E_WAVE_INVALID;
		public EffAnim<DefEff> A_WAVE_STOP;
		public EffAnim<DefEff> A_E_WAVE_STOP;
		public EffAnim<DefEff> A_WAVEGUARD;// unused
		public EffAnim<DefEff> A_E_WAVEGUARD;// unused
		public EffAnim<DefEff> A_EFF_INV;
		public EffAnim<DefEff> A_EFF_DEF;// unused
		public EffAnim<DefEff> A_Z_STRONG;
		public EffAnim<BarrierEff> A_B;
		public EffAnim<BarEneEff> A_E_B;
		public EffAnim<WarpEff> A_W;
		public EffAnim<WarpEff> A_W_C;
		public EffAnim<DefEff> A_CURSE;
		public EffAnim<ZombieEff> A_ZOMBIE;
		public EffAnim<DefEff> A_SHOCKWAVE;
		public EffAnim<DefEff> A_CRIT;
		public EffAnim<KBEff> A_KB;
		public EffAnim<SniperEff> A_SNIPER;
		public EffAnim<ZombieEff> A_U_ZOMBIE;
		public EffAnim<BarrierEff> A_U_B;
		public EffAnim<BarEneEff> A_U_E_B;
		public EffAnim<DefEff> A_SEAL;
		public EffAnim<DefEff> A_POI0;
		public EffAnim<DefEff> A_POI1;
		public EffAnim<DefEff> A_POI2;
		public EffAnim<DefEff> A_POI3;
		public EffAnim<DefEff> A_POI4;
		public EffAnim<DefEff> A_POI5;
		public EffAnim<DefEff> A_POI6;
		public EffAnim<DefEff> A_POI7;
		public EffAnim<DefEff> A_SATK;
		public EffAnim<DefEff> A_IMUATK;
		public EffAnim<DefEff> A_POISON;
		public EffAnim<VolcEff> A_VOLC;
		public EffAnim<VolcEff> A_E_VOLC;
		public EffAnim<DefEff> A_E_CURSE;
		public EffAnim<DefEff> A_WAVE;
		public EffAnim<DefEff> A_E_WAVE;
		public EffAnim<ArmorEff> A_ARMOR;
		public EffAnim<ArmorEff> A_E_ARMOR;
		public EffAnim<SpeedEff> A_SPEED;
		public EffAnim<SpeedEff> A_E_SPEED;
		public EffAnim<WeakUpEff> A_WEAK_UP;
		public EffAnim<WeakUpEff> A_E_WEAK_UP;

		public EffAnim<?>[] values() {
			Field[] fld = EffAnimStore.class.getDeclaredFields();
			EffAnim<?>[] ans = new EffAnim[fld.length];
			Data.err(() -> {
				for (int i = 0; i < ans.length; i++)
					ans[i] = (EffAnim<?>) fld[i].get(this);
			});
			return ans;
		}

		private void set(int i, EffAnim<DefEff> eff) {
			err(() -> EffAnimStore.class.getDeclaredFields()[i].set(this, eff));
		}

	}

	public interface EffType<T extends Enum<T> & EffType<T>> extends AnimI.AnimType<EffAnim<T>, T> {
		String path();
	}

	public enum KBEff implements EffType<KBEff> {
		KB("_hb"), SW("_sw"), ASS("_ass");

		private final String path;

		KBEff(String str) {
			path = str;
		}

		@Override
		public String path() {
			return path;
		}

	}

	public enum SniperEff implements EffType<SniperEff> {
		IDLE("00"), ATK("01");

		private final String path;

		SniperEff(String str) {
			path = str;
		}

		@Override
		public String path() {
			return path;
		}
	}

	public enum SpeedEff implements EffType<SpeedEff> {
		UP("up"), DOWN("down");

		private final String path;

		SpeedEff(String str) {
			path = str;
		}

		@Override
		public String path() {
			return path;
		}
	}

	public enum VolcEff implements EffType<VolcEff> {
		START("00"), DURING("01"), END("02");

		private final String path;

		VolcEff(String str) {
			path = str;
		}

		@Override
		public String path() {
			return path;
		}
	}

	public enum WarpEff implements EffType<WarpEff> {
		ENTER("_entrance"), EXIT("_exit");

		private final String path;

		WarpEff(String str) {
			path = str;
		}

		@Override
		public String path() {
			return path;
		}
	}

	public enum WeakUpEff implements EffType<WeakUpEff> {
		UP("up");

		private final String path;

		WeakUpEff(String str) {
			path = str;
		}

		@Override
		public String path() {
			return path;
		}
	}

	public enum ZombieEff implements EffType<ZombieEff> {
		REVIVE("_revive"), DOWN("_down");

		private final String path;

		ZombieEff(String str) {
			path = str;
		}

		@Override
		public String path() {
			return path;
		}
	}

	public static void read() {
		EffAnimStore effas = CommonStatic.getBCAssets().effas;
		String stre = "./org/battle/e1/set_enemy001_zombie";
		VImg ve = new VImg(stre + ".png");
		ImgCut ice = ImgCut.newIns(stre + ".imgcut");
		String stra = "./org/battle/a/";
		VImg va = new VImg(stra + "000_a.png");
		ImgCut ica = ImgCut.newIns(stra + "000_a.imgcut");
		String ski = "skill00";
		String[] stfs = new String[4];
		VImg[] vfs = new VImg[4];
		ImgCut[] icfs = new ImgCut[4];
		for (int i = 0; i < 4; i++) {
			stfs[i] = "./org/battle/s" + i + "/";
			vfs[i] = new VImg(stfs[i] + ski + i + ".png");
			icfs[i] = ImgCut.newIns(stfs[i] + ski + i + ".imgcut");
		}
		effas.A_SHOCKWAVE = new EffAnim<>(stra + "boss_welcome", va, ica, DefEff.values());
		effas.A_CRIT = new EffAnim<>(stra + "critical", va, ica, DefEff.values());
		effas.A_KB = new EffAnim<>(stra + "kb", va, ica, KBEff.values());
		effas.A_ZOMBIE = new EffAnim<>(stre, ve, ice, ZombieEff.values());
		effas.A_U_ZOMBIE = new EffAnim<>(stre, ve, ice, ZombieEff.values());
		effas.A_U_ZOMBIE.rev = true;
		ski = "skill_";
		for (int i = 0; i < A_PATH.length; i++) {
			String path = stfs[0] + A_PATH[i] + "/" + ski + A_PATH[i];
			effas.set(i * 2, new EffAnim<>(path, vfs[0], icfs[0], DefEff.values()));
			effas.set(i * 2 + 1, new EffAnim<>(path + "_e", vfs[0], icfs[0], DefEff.values()));
		}
		effas.A_EFF_INV = new EffAnim<>(stfs[0] + ski + "effect_invalid", vfs[0], icfs[0], DefEff.values());
		effas.A_EFF_DEF = new EffAnim<>(stfs[0] + ski + "effectdef", vfs[0], icfs[0], DefEff.values());
		effas.A_Z_STRONG = new EffAnim<>(stfs[1] + ski + "zombie_strong", vfs[1], icfs[1], DefEff.values());
		effas.A_B = new EffAnim<>(stfs[2] + ski + "barrier", vfs[2], icfs[2], BarrierEff.values());
		effas.A_U_B = new EffAnim<>(stfs[2] + ski + "barrier", vfs[2], icfs[2], BarrierEff.values());
		effas.A_U_B.rev = true;
		effas.A_E_B = new EffAnim<>(stfs[2] + ski + "barrier_e", vfs[2], icfs[2], BarEneEff.values());
		effas.A_U_E_B = new EffAnim<>(stfs[2] + ski + "barrier_e", vfs[2], icfs[2], BarEneEff.values());
		effas.A_U_E_B.rev = true;
		effas.A_W = new EffAnim<>(stfs[2] + ski + "warp", vfs[2], icfs[2], WarpEff.values());
		effas.A_W_C = new EffAnim<>(stfs[2] + ski + "warp_chara", vfs[2], icfs[2], WarpEff.values());
		String strs = "./org/battle/sniper/";
		String strm = "img043";
		VImg vis = new VImg(strs + strm + ".png");
		ImgCut ics = ImgCut.newIns(strs + strm + ".imgcut");
		effas.A_SNIPER = new EffAnim<>(strs + "000_snyaipa", vis, ics, SniperEff.values());
		effas.A_CURSE = new EffAnim<>(stfs[3] + ski + "curse", vfs[3], icfs[3], DefEff.values());

		readCustom(stfs, icfs);

		VImg vuw = new VImg("./org/battle/s4/skill004.png");
		ImgCut icsvuw = ImgCut.newIns("./org/battle/s4/skill004.imgcut");
		effas.A_WAVE = new EffAnim<>("./org/battle/s4/skill_wave_attack", vuw, icsvuw, DefEff.values());
		VImg vew = new VImg("./org/battle/s5/skill005.png");
		ImgCut icsvew = ImgCut.newIns("./org/battle/s5/skill005.imgcut");
		effas.A_E_WAVE = new EffAnim<>("./org/battle/s5/skill_wave_attack_e", vew, icsvew, DefEff.values());
		VImg vsatk = new VImg("./org/battle/s6/skill006.png");
		ImgCut icsatk = ImgCut.newIns("./org/battle/s6/skill006.imgcut");
		effas.A_SATK = new EffAnim<>("./org/battle/s6/strong_attack", vsatk, icsatk, DefEff.values());
		VImg viatk = new VImg("./org/battle/s7/skill007.png");
		ImgCut iciatk = ImgCut.newIns("./org/battle/s7/skill007.imgcut");
		effas.A_IMUATK = new EffAnim<>("./org/battle/s7/skill_attack_invalid", viatk, iciatk, DefEff.values());
		VImg vip = new VImg("./org/battle/s8/skill008.png");
		ImgCut icp = ImgCut.newIns("./org/battle/s8/skill008.imgcut");
		effas.A_POISON = new EffAnim<>("./org/battle/s8/skill_percentage_attack", vip, icp, DefEff.values());
		VImg vic = new VImg("./org/battle/s9/skill009.png");
		ImgCut icc = ImgCut.newIns("./org/battle/s9/skill009.imgcut");
		effas.A_VOLC = new EffAnim<>("./org/battle/s9/skill_volcano", vic, icc, VolcEff.values());
		vic = new VImg("./org/battle/s10/skill010.png");
		icc = ImgCut.newIns("./org/battle/s10/skill010.imgcut");
		effas.A_E_VOLC = new EffAnim<>("./org/battle/s10/skill_volcano", vic, icc, VolcEff.values());
		VImg vcu = new VImg("./org/battle/s11/skill011.png");
		ImgCut iccu = ImgCut.newIns("./org/battle/s11/skill011.imgcut");
		effas.A_E_CURSE = new EffAnim<>("./org/battle/s11/skill_curse_e", vcu, iccu, DefEff.values());
	}

	private static void excColor(FakeImage fimg, Function<int[], Integer> f) {
		fimg.mark(Marker.RECOLOR);
		int w = fimg.getWidth();
		int h = fimg.getHeight();
		for (int i = 0; i < w; i++)
			for (int j = 0; j < h; j++) {
				int p = fimg.getRGB(i, j);
				int b = p & 255;
				int g = p >> 8 & 255;
				int r = p >> 16 & 255;
				int a = p >> 24;
				p = f.apply(new int[] { a, r, g, b });
				fimg.setRGB(i, j, p);
			}
		fimg.mark(Marker.RECOLORED);
	}

	private static void readCustom(String[] stfs, ImgCut[] icfs) {
		String ski = "skill_";
		EffAnimStore effas = CommonStatic.getBCAssets().effas;

		VImg vseal = new VImg(stfs[3] + "skill003.png");
		excColor(vseal.getImg(), (is) -> (is[0] << 24 | is[1] << 16 | is[3] << 8 | is[2]));
		effas.A_SEAL = new EffAnim<>(stfs[3] + ski + "curse", vseal, icfs[3], DefEff.values());
		VImg vpois = new VImg(stfs[3] + "skill003.png");
		excColor(vpois.getImg(), (is) -> (is[0] << 24 | is[2] << 16 | is[3] << 8 | is[1]));
		effas.A_POI0 = new EffAnim<>(stfs[3] + ski + "curse", vpois, icfs[3], DefEff.values());
		effas.A_POI0.name = "poison_DF";
		vpois = new VImg(stfs[3] + "poison.png");
		effas.A_POI1 = new EffAnim<>(stfs[3] + ski + "curse", vpois, icfs[3], DefEff.values());
		effas.A_POI1.name = "poison_DT0";
		String strpb = stfs[3] + "poisbub/poisbub";
		vpois = new VImg(strpb + ".png");
		ImgCut icpois = ImgCut.newIns(strpb + ".imgcut");
		effas.A_POI2 = new EffAnim<>(strpb, vpois, icpois, DefEff.values());
		effas.A_POI2.name = "poison_purple";
		vpois = new VImg(strpb + ".png");
		excColor(vpois.getImg(), (is) -> (is[0] << 24 | is[1] << 16 | is[3] << 8 | is[2]));
		effas.A_POI3 = new EffAnim<>(strpb, vpois, icpois, DefEff.values());
		effas.A_POI3.name = "poison_green";
		vpois = new VImg(strpb + ".png");
		excColor(vpois.getImg(), (is) -> (is[0] << 24 | is[2] << 16 | is[1] << 8 | is[3]));
		effas.A_POI4 = new EffAnim<>(strpb, vpois, icpois, DefEff.values());
		effas.A_POI4.name = "poison_blue";
		vpois = new VImg(strpb + ".png");
		excColor(vpois.getImg(), (is) -> (is[0] << 24 | is[2] << 16 | is[3] << 8 | is[1]));
		effas.A_POI5 = new EffAnim<>(strpb, vpois, icpois, DefEff.values());
		effas.A_POI5.name = "poison_cyan";
		vpois = new VImg(strpb + ".png");
		excColor(vpois.getImg(), (is) -> (is[0] << 24 | is[3] << 16 | is[1] << 8 | is[2]));
		effas.A_POI6 = new EffAnim<>(strpb, vpois, icpois, DefEff.values());
		effas.A_POI6.name = "poison_orange";
		vpois = new VImg(strpb + ".png");
		excColor(vpois.getImg(), (is) -> (is[0] << 24 | is[3] << 16 | is[2] << 8 | is[1]));
		effas.A_POI7 = new EffAnim<>(strpb, vpois, icpois, DefEff.values());
		effas.A_POI7.name = "poison_pink";

		String breaker = stfs[3] + "armor_break/armor_break";
		VImg vbreak = new VImg(breaker + ".png");
		ImgCut icbreak = ImgCut.newIns(breaker + ".imgcut");
		effas.A_ARMOR = new EffAnim<>(breaker, vbreak, icbreak, ArmorEff.values());
		breaker = stfs[3] + "armor_break_e/armor_break_e";
		icbreak = ImgCut.newIns(breaker + ".imgcut");
		vbreak = new VImg(breaker + ".png");
		effas.A_E_ARMOR = new EffAnim<>(breaker, vbreak, icbreak, ArmorEff.values());

		String speed = stfs[3] + "speed/speed";
		VImg vspeed = new VImg(speed + ".png");
		ImgCut icspeed = ImgCut.newIns(speed + ".imgcut");
		effas.A_SPEED = new EffAnim<>(speed, vspeed, icspeed, SpeedEff.values());
		speed = stfs[3] + "speed_e/speed_e";
		vspeed = new VImg(speed + ".png");
		icspeed = ImgCut.newIns(speed + ".imgcut");
		effas.A_E_SPEED = new EffAnim<>(speed, vspeed, icspeed, SpeedEff.values());

		String wea = "./org/battle/";
		String weakup = wea + "weaken_up/weaken_up";
		VImg vwea = new VImg(weakup + ".png");
		ImgCut icwea = ImgCut.newIns(weakup + ".imgcut");
		effas.A_WEAK_UP = new EffAnim<>(weakup, vwea, icwea, WeakUpEff.values());

		weakup = wea + "weaken_up_e/weaken_up_e";
		vwea = new VImg(weakup + ".png");
		icwea = ImgCut.newIns(weakup + ".imgcut");
		effas.A_E_WEAK_UP = new EffAnim<>(weakup, vwea, icwea, WeakUpEff.values());
	}

	private final VImg vimg;
	private boolean rev;
	private String name = "";

	public EffAnim(String st, VImg vi, ImgCut ic, T[] anims) {
		super(st);
		vimg = vi;
		imgcut = ic;
		types = anims;

	}

	@Override
	public FakeImage getNum() {
		return vimg.getImg();
	}

	@Override
	public void load() {
		loaded = true;
		parts = imgcut.cut(vimg.getImg());
		mamodel = MaModel.newIns(str + ".mamodel");
		anims = new MaAnim[types.length];
		for (int i = 0; i < types.length; i++)
			anims[i] = MaAnim.newIns(str + types[i].path() + ".maanim");
		if (rev)
			revert();
	}

	@Override
	public String toString() {
		if (name.length() > 0)
			return name;
		String[] ss = str.split("/");
		return ss[ss.length - 1];
	}

}
