package common.util.anim;

import common.io.assets.Admin.StaticPermitted;
import common.system.VImg;
import common.system.fake.FakeImage;

import java.util.Arrays;

public abstract class AnimU<T extends AnimU.ImageKeeper> extends AnimD<AnimU<?>, AnimU.UType> {

	public interface EditableType {
		boolean rotate();
	}

	public interface ImageKeeper {

		VImg getEdi();

		ImgCut getIC();

		MaAnim[] getMA();

		MaModel getMM();

		FakeImage getNum();

		VImg getUni();

		void unload();

	}

	public static class UType implements AnimI.AnimType<AnimU<?>, UType>, EditableType {
		private String name;
		private final boolean rotate;

		UType(String name, boolean rotate) {
			this.name = name;
			this.rotate = rotate;
		}

		public void changeName(String str) {
			name = str;
		}
		@Override
		public boolean rotate() {
			return rotate;
		}
		@Override
		public String toString() {
			return name;
		}
	}

	public static final int WALK = 0, IDLE = 1, HB = 3, BURROW_DOWN = 4, UNDERGROUND = 5, BURROW_UP = 6, ENTRY = 7, RETREAT = 8;
	@StaticPermitted
	public static final UType[] TYPEDEF = { new UType("walk", false), new UType("idle", false), new UType("attack", true),
			new UType("kb", false), new UType("burrow_down", true), new UType("burrow_move", false), new UType("burrow_up", true),
			new UType("entry", true), new UType("retreat", false) };

	@StaticPermitted
	public static final UType[] SOUL = { new UType("soul", true) };
	@StaticPermitted
	public static final UType[] BGEFFECT = { new UType("background", false), new UType("foreground", false) };

	protected boolean partial = false;
	public final T loader;

	protected AnimU(String path, T load) {
		super(path);
		loader = load;
	}

	protected AnimU(T load) {
		super("");
		loader = load;
	}

	public int getAtkCount() {
		if (types.length < TYPEDEF.length)
			return 0;
		return types.length - TYPEDEF.length + 1;
	}

	public int getAtkLen(int atk) {
		partial();
		return anims[2 + atk].len + 1;
	}

	public void addAttack() {
		int ind = 2 + getAtkCount();
		MaAnim[] newMaAnim = new MaAnim[anims.length + 1];
		UType[] newUType = new UType[newMaAnim.length];

		for (int i = 0; i < newMaAnim.length; i++) {
			if (i == ind)
				i++;
			if (i < ind) {
				newMaAnim[i] = anims[i];
				newUType[i] = types[i];
			} else {
				newMaAnim[i] = anims[i - 1];
				newUType[i] = types[i - 1];
			}
		}
		anims = newMaAnim;
		types = newUType;
		anims[ind] = new MaAnim();
		types[ind] = new UType("attack" + (ind - 2), true);
	}

	public void remAttack(int atk) {
		MaAnim[] newMaAnim = new MaAnim[anims.length - 1];
		UType[] newUType = new UType[newMaAnim.length];

		for (int i = 0; i < newMaAnim.length; i++) {
			if (i < atk) {
				newMaAnim[i] = anims[i];
				newUType[i] = types[i];
			} else {
				newMaAnim[i] = anims[i + 1];
				newUType[i] = types[i + 1];
			}
		}
		newUType[2] = TYPEDEF[2];
		for (int i = 3; i < newUType.length - 6; i++)
			newUType[i].changeName("attack" + (i - 2));
		anims = newMaAnim;
		types = newUType;
	}

	@Override
	public EAnimU getEAnim(UType t) {
		check();
		return new EAnimU(this, t);
	}

	public VImg getEdi() {
		return loader.getEdi();
	}

	@Override
	public FakeImage getNum() {
		return loader.getNum();
	}

	public VImg getUni() {
		return loader.getUni();
	}

	@Override
	public void load() {
		loaded = true;
		try {
			imgcut = loader.getIC();
			if (getNum() == null) {
				mamodel = null;
				return;
			}
			parts = imgcut.cut(getNum());
			partial();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public final String[] names() {
		check();
		String[] str = translate(types);
		for (int i = 3; i < str.length - 6; i++) {
			str[i] = str[2] + " " + (i - 2);
		}
		return str;
	}

	@Override
	public void unload() {
		loader.unload();
		super.unload();
	}

	public void partial() {
		if (!partial) {
			partial = true;
			imgcut = loader.getIC();
			mamodel = loader.getMM();
			anims = loader.getMA();
			types = new UType[anims.length];
			if (types.length <= TYPEDEF.length) {
				types = types.length == 1 ? SOUL : types.length == 2 ? BGEFFECT : types.length == TYPEDEF.length ? TYPEDEF : Arrays.copyOf(TYPEDEF, types.length);
			} else {
				for (int i = 0; i < types.length; i++) {
					if (i < 3)
						types[i] = TYPEDEF[i];
					else if (i >=  anims.length - 6)
						types[i] = TYPEDEF[i - (anims.length - TYPEDEF.length)];
					else
						types[i] = new UType("attack" + (i - 2), true);
				}
			}
		}
	}

}
