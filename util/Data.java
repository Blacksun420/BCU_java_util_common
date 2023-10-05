package common.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import common.CommonStatic;
import common.io.assets.Admin.StaticPermitted;
import common.io.json.*;
import common.io.json.FieldOrder.Order;
import common.io.json.JsonClass.NoTag;
import common.pack.Context.ErrType;
import common.pack.Context.RunExc;
import common.pack.Context.SupExc;
import common.pack.Identifier;
import common.util.pack.Background;
import common.util.pack.EffAnim;
import common.util.stage.Music;

import java.lang.annotation.*;
import java.lang.reflect.Field;

@StaticPermitted
public class Data {

	public static final Proc empty = Proc.blank();
	@JsonClass(read = JsonClass.RType.MANUAL, write = JsonClass.WType.CLASS, generator = "genProc", serializer = "serProc")
	public static class Proc implements BattleStatic {

		@JsonClass(noTag = NoTag.LOAD)
		public static class PROB extends ProcItem {
			@Order(0)
			public int prob;

			@Override
			public boolean perform(CopRand r) {
				return exists() && (prob == 100 || r.nextDouble() * 100 < prob);
			}
			@Override
			public boolean exists() {
				return prob != 0;
			}
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class MULT extends ProcItem {
			@Order(0)
			public int mult;
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class PM extends PROB {
			@Order(1)
			public int mult;
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class PT extends PROB {
			@Order(1)
			public int time;
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class PTD extends PT {
			@Order(2)
			public int dis;
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class PTM extends PT {
			@Order(2)
			public int mult;
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class IMU extends MULT {
			@Order(1)
			public int block;
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class IMUAD extends IMU {
			@Order(2)
			public int smartImu;
		}
		@JsonClass(noTag = NoTag.LOAD)
		public static class WAVE extends ProcItem {
			@JsonClass(noTag = NoTag.LOAD)
			public static class TYPE extends IntType {
				@Order(0)
				public boolean hitless;
			}
			@Order(0)
			public int prob;
			@Order(1)
			public int lv;
			@Order(2)
			public TYPE type = new TYPE();
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class MINIWAVE extends ProcItem {
			@JsonClass(noTag = NoTag.LOAD)
			public static class TYPE extends IntType {
				@Order(0)
				public boolean hitless;
			}
			@Order(0)
			public int prob;
			@Order(1)
			public int lv;
			@Order(2)
			public int multi;
			@Order(3)
			public TYPE type = new TYPE();
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class CANNI extends MULT {
			@Order(1)
			public int type;
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class VOLC extends ProcItem {
			@JsonClass(noTag = NoTag.LOAD)
			public static class TYPE extends IntType {
				@Order(0)
				public boolean hitless;
			}
			@Order(0)
			public int prob;
			@Order(1)
			public int dis_0;
			@Order(2)
			public int dis_1;
			@Order(3)
			public int time;
			@Order(4)
			public TYPE type = new TYPE();
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class MINIVOLC extends ProcItem {
			@JsonClass(noTag = NoTag.LOAD)
			public static class TYPE extends IntType {
				@Order(0)
				public boolean hitless;
			}
			@Order(0)
			public int prob;
			@Order(1)
			public int dis_0;
			@Order(2)
			public int dis_1;
			@Order(3)
			public int time;
			@Order(4)
			public int mult;
			@Order(5)
			public TYPE type = new TYPE();
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class STRONG extends ProcItem {
			@Order(0)
			public int health;
			@Order(1)
			public int mult;
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class BURROW extends ProcItem {
			@Order(0)
			public int count;
			@Order(1)
			public int dis;
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class REVIVE extends ProcItem {

			@JsonClass(noTag = NoTag.LOAD)
			public static class TYPE extends IntType {
				@BitCount(2)
				@Order(0)
				public int range_type;
				@Order(1)
				public boolean imu_zkill;
				@Order(2)
				public boolean revive_non_zombie;
				@Order(3)
				public boolean revive_others;
			}

			@Order(0)
			public int count;
			@Order(1)
			public int time;
			@Order(2)
			public int health;
			@Order(3)
			public int dis_0;
			@Order(4)
			public int dis_1;
			@Order(5)
			public TYPE type = new TYPE();
		}

		@JsonClass(noTag = NoTag.LOAD) // Starred Barrier
		public static class BARRIER extends ProcItem {

			@JsonClass(noTag = NoTag.LOAD)
			public static class TYPE extends IntType {
				@Order(0)
				public boolean magnif;
			}
			@Order(0)
			public int health;
			@Order(1)
			public int regentime;
			@Order(2)
			public int timeout;
			@Order(3)
			public TYPE type = new TYPE();
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class DSHIELD extends ProcItem {
			@Order(0)
			public int hp;
			@Order(1)
			public int regen;
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class BSTHUNT extends ProcItem {
			@JsonClass(noTag = NoTag.LOAD)
			public static class TYPE extends IntType {
				@Order(0)
				public boolean active;
			}
			@Order(0)
			public TYPE type = new TYPE();
			@Order(1)
			public int prob;
			@Order(2)
			public int time;
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class CDSETTER extends ProcItem {
			@Order(0)
			public int prob;
			@Order(1)
			public int amount;
			@Order(2)
			public int slot;
			@Order(3)
			public int type; //0 - frames, 1 - %, 2 - set
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class AURA extends ProcItem {
			@JsonClass(noTag = NoTag.LOAD)
			public static class TYPE extends IntType {
				@Order(0)
				public boolean trait; //classic ignore/consider trait
			}
			@Order(0)
			public int amult; //Modifies Damage
			@Order(1)
			public int dmult; //Modifies Defense
			@Order(2)
			public int smult; //Modifies Speed
			@Order(3)
			public int tmult; //Modifies TBA
			@Order(4)
			public int min_dis;
			@Order(5)
			public int max_dis;
			@Order(6)
			public TYPE type = new TYPE();
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class LETHARGY extends PTM {
			@JsonClass(noTag = NoTag.LOAD)
			public static class TYPE extends IntType {
				@Order(0)
				public boolean percentage;
			}
			@Order(3)
			public TYPE type = new TYPE();
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class MOVEWAVE extends ProcItem {
			@Order(0)
			public int prob;
			@Order(1)
			public int speed;
			@Order(2)
			public int width;
			@Order(3)
			public int time;
			@Order(4)
			public int dis;
			@Order(5)
			public int itv;
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class POISON extends ProcItem {

			@JsonClass(noTag = NoTag.LOAD)
			public static class TYPE extends IntType {
				@BitCount(2)
				@Order(0)
				public int damage_type;
				@Order(1)
				public boolean unstackable;
				@Order(2)
				public boolean ignoreMetal;
				@Order(3)
				public boolean modifAffected;
			}

			@Order(0)
			public int prob;
			@Order(1)
			public int time;
			@Order(2)
			public int damage;
			@Order(3)
			public int itv;
			@Order(4)
			public TYPE type = new TYPE();
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class WARP extends PT {
			@Order(2)
			public int dis;
			@Order(3)
			public int dis_1;
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class TIME extends PT {
			@Order(2)
			public int intensity;
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class SPEED extends PT {
			@Order(2)
			public int speed;
			@Order(3)
			public int type;
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class SUMMON extends ProcItem {

			@JsonClass(noTag = NoTag.LOAD)
			public static class TYPE extends IntType {
				@BitCount(2)
				@Order(0)
				public int anim_type;
				@Order(1)
				public boolean ignore_limit;
				@Order(2)
				public boolean fix_buff;
				@Order(3)
				public boolean same_health;
				@Order(4)
				public boolean bond_hp;
				@Order(5)
				public boolean on_hit;
				@Order(6)
				public boolean on_kill;
				@BitCount(2)
				@Order(7)
				public int pass_proc;
			}

			@Order(0)
			public int prob;
			@Order(1)
			public Identifier<?> id;
			@Order(2)
			public int dis;
			@Order(3)
			public int max_dis;
			@Order(4)
			public int mult;
			@Order(5)
			public int min_layer;
			@Order(6)
			public int max_layer;
			@Order(7)
			public TYPE type = new TYPE();
			@Order(8)
			public int time;
			@Order(9)
			public int amount;
			@Order(10)
			public int form;
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class THEME extends PT {

			@JsonClass(noTag = NoTag.LOAD)
			public static class TYPE extends IntType {
				@Order(0)
				public boolean kill;
			}
			@Order(2)
			public Identifier<Background> id;
			@Order(3)
			public Identifier<Music> mus;
			@Order(4)
			public TYPE type = new TYPE();
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class COUNTER extends ProcItem {
			@JsonClass(noTag = NoTag.LOAD)
			public static class TYPE extends IntType {
				@BitCount(2)
				@Order(0)
				public int counterWave;
				@BitCount(2)
				@Order(1)
				public int procType;
				@Order(1)
				public boolean useOwnDamage;
				@Order(2)
				public boolean outRange;
				@Order(3)
				public boolean areaAttack;
			}

			@Order(0)
			public int prob;
			@Order(1)
			public int damage;
			@Order(2)
			public int minRange;
			@Order(3)
			public int maxRange;
			@Order(4)
			public TYPE type = new TYPE();
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class DMGCUT extends ProcItem {
			@JsonClass(noTag = NoTag.LOAD)
			public static class TYPE extends IntType {
				@Order(0)
				public boolean traitIgnore;
				@Order(1)
				public boolean procs;
				@Order(2)
				public boolean magnif;
			}
			@Order(0)
			public int prob;
			@Order(1)
			public int dmg;
			@Order(2)
			public int reduction;
			@Order(3)
			public TYPE type = new TYPE();
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class DMGCAP extends ProcItem {
			@JsonClass(noTag = NoTag.LOAD)
			public static class TYPE extends IntType {
				@Order(0)
				public boolean traitIgnore;
				@Order(1)
				public boolean nullify;
				@Order(2)
				public boolean procs;
				@Order(3)
				public boolean magnif;
			}
			@Order(0)
			public int prob;
			@Order(1)
			public int dmg;
			@Order(2)
			public TYPE type = new TYPE();
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class REMOTESHIELD extends ProcItem {
			@JsonClass(noTag = NoTag.LOAD)
			public static class TYPE extends IntType {
				@Order(0)
				public boolean traitCon;
				@Order(1)
				public boolean procs;
				@Order(2)
				public boolean waves;
			}
			@Order(0)
			public int prob;
			@Order(1)
			public int minrange;
			@Order(2)
			public int maxrange;
			@Order(3)
			public int reduction;
			@Order(4)
			public int block;
			@Order(5)
			public TYPE type = new TYPE();
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class AI extends ProcItem {
			@JsonClass(noTag = NoTag.LOAD)
			public static class TYPE extends IntType {
				@Order(0)
				public boolean calcstrongest;
				@Order(1)
				public boolean calcblindspot;
			}
			@Order(0)
			public int retreatDist;
			@Order(1)
			public int retreatSpeed;
			@Order(2)
			public TYPE type = new TYPE();
		}

		@JsonClass(noTag = NoTag.LOAD)
		public static class RANGESHIELD extends PM {
			@JsonClass(noTag = NoTag.LOAD)
			public static class TYPE extends IntType {
				@Order(0)
				public boolean range;
			}
			@Order(2)
			public TYPE type = new TYPE();
		}

		public static abstract class IntType implements Cloneable, BattleStatic {

			@Documented
			@Retention(value = RetentionPolicy.RUNTIME)
			@Target(value = ElementType.FIELD)
			public @interface BitCount {
				int value();
			}

			@Override
			public IntType clone() throws CloneNotSupportedException {
				return (IntType) super.clone();
			}

			public Field[] getDeclaredFields() {
				return FieldOrder.getDeclaredFields(this.getClass());
			}

			public void set(int i, int v) {
				try {
					Field fs = getDeclaredFields()[i];
					if (fs.getType() == int.class)
						fs.set(this, v);
					else if (fs.getType() == boolean.class)
						fs.set(this, v != 0);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			public IntType load(int val) throws Exception {
				Field[] fs = getDeclaredFields();
				for (int i = 0; i < fs.length;) {
					BitCount c = fs[i].getAnnotation(BitCount.class);
					if (c == null) {
						fs[i].set(this, (val >> i & 1) == 1);
						i++;
					} else {
						fs[i].set(this, val >> i & (1 << c.value()) - 1);
						i += c.value();
					}
				}
				return this;
			}

			public int toInt() throws Exception {
				Field[] fs = getDeclaredFields();
				int ans = 0;
				for (int i = 0; i < fs.length;) {
					BitCount c = fs[i].getAnnotation(BitCount.class);
					if (c == null) {
						if (fs[i].getBoolean(this))
							ans |= 1 << i;
						i++;
					} else {
						int val = fs[i].getInt(this);
						ans |= val << i;
						i += c.value();
					}
				}
				return ans;
			}

		}

		public static abstract class ProcItem implements Cloneable, BattleStatic {
			public ProcItem clear() {
				try {
					Field[] fs = getDeclaredFields();
					for (Field f : fs)
						if (f.getType() == int.class)
							f.set(this, 0);
						else if (IntType.class.isAssignableFrom(f.getType()))
							f.set(this, (f.getType().getDeclaredConstructor().newInstance()));
						else if (f.getType() == Identifier.class)
							f.set(this, null);
						else
							throw new Exception("unknown field " + f.getType() + " " + f.getName());
				} catch (Exception e) {
					e.printStackTrace();
				}
				return this;
			}

			@Override
			public ProcItem clone() {
				try {
					ProcItem ans = (ProcItem) super.clone();
					Field[] fs = getDeclaredFields();
					for (Field f : fs)
						if (IntType.class.isAssignableFrom(f.getType()) && f.get(this) != null) {
							f.set(ans, ((IntType) f.get(this)).clone());
						} else if (f.getType() == Identifier.class && f.get(this) != null)
							f.set(ans, ((Identifier<?>) f.get(this)).clone());
					return ans;
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}

			public boolean exists() {
				try {
					Field[] fs = getDeclaredFields();
					for (Field f : fs)
						if (f.getType() == int.class) {
							Object o = f.get(this);

							if(f.getName().equals("prob") && ((Integer) o) == 0)
								return false;

							if (((Integer) o) != 0)
								return true;
						} else {
							if (((IntType) f.get(this)).toInt() > 0)
								return true;
						}
				} catch (Exception e) {
					e.printStackTrace();
				}
				return false;
			}

			public int get(int i) {
				try {
					Field f = getDeclaredFields()[i];
					return f.getType() == int.class ? f.getInt(this) : ((IntType) f.get(this)).toInt();
				} catch (Exception e) {
					e.printStackTrace();
					return 0;
				}
			}

			public String getFieldName(int i) {
				try {
					Field f = getDeclaredFields()[i];

					return f.getName();
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}

			public Field[] getDeclaredFields() {
				return FieldOrder.getFields(this.getClass());
			}

			public boolean perform(CopRand r) {
				try {
					Field f = get("prob");
					int prob = f.getInt(this);
					if (prob == 0)
						return false;
					if (prob == 100)
						return true;
					return r.nextFloat() * 100 < prob;
				} catch (Exception e) {
					return exists();
				}
			}

			public Field get(String name) {
				try {
					return this.getClass().getField(name);
				} catch (Exception ignored) {
					return null;
				}
			}

			/**
			 * should not modify Identifier, used for talents only
			 */
			@Deprecated
			public void set(int i, int v) {
				try {
					Field[] fs = getDeclaredFields();
					int loc = 0, lastloc = 0;
					for (int j = 0; j < fs.length && loc < i; j++)
						if (IntType.class.isAssignableFrom(fs[j].getType())) {
							int len = ((IntType) fs[j].get(this)).getDeclaredFields().length - 1;
							if (j + loc + len >= i) {
								lastloc = i - j - loc;
								break;
							}
							loc += len;
						}
					Field f = fs[i - loc];
					if (f.getType() == int.class)
						f.set(this, v);
					else if (IntType.class.isAssignableFrom(f.getType()))
						((IntType)f.get(this)).set(lastloc, v);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			public void set(ProcItem pi) {
				try {
					Field[] fs = getDeclaredFields();
					for (Field f : fs)
						if (f.getType().isPrimitive())
							f.set(this, f.get(pi));
						else if (IntType.class.isAssignableFrom(f.getType()))
							f.set(this, ((IntType) f.get(pi)).clone());
						else if (f.getType() == Identifier.class) {
							Identifier<?> id = (Identifier<?>) f.get(pi);
							f.set(this, id == null ? null : id.clone());
						} else
							throw new Exception("unknown field " + f.getType() + " " + f.getName());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public String toString() {
				return JsonEncoder.encode(this).toString();
			}

		}

		public static Proc blank() {
			return new Proc();
		}

		public static Field[] getDeclaredFields() {
			return FieldOrder.getFields(Proc.class);
		}

		public static String getName(int i) {
			return getDeclaredFields()[i].getName();
		}

		@Order(0)
		public final PTD KB = new PTD();
		@Order(1)
		public final PT STOP = new PT();
		@Order(2)
		public final PT SLOW = new PT();
		@Order(3)
		public final PM CRIT = new PM();
		@Order(4)
		public final WAVE WAVE = new WAVE();
		@Order(5)
		public final MINIWAVE MINIWAVE = new MINIWAVE();
		@Order(6)
		public final MOVEWAVE MOVEWAVE = new MOVEWAVE();
		@Order(7)
		public final VOLC VOLC = new VOLC();
		@Order(8)
		public final PTM WEAK = new PTM();
		@Order(9)
		public final PROB BREAK = new PROB();
		@Order(10)
		public final PROB SHIELDBREAK = new PROB();
		@Order(11)
		public final WARP WARP = new WARP();
		@Order(12)
		public final PT CURSE = new PT();
		@Order(13)
		public final PT SEAL = new PT();
		@Order(14)
		public final SUMMON SUMMON = new SUMMON();
		@Order(15)
		public final TIME TIME = new TIME();
		@Order(16)
		public final PROB SNIPER = new PROB();
		@Order(17)
		public final THEME THEME = new THEME();
		@Order(18)
		public final PROB BOSS = new PROB();
		@Order(19)
		public final POISON POISON = new POISON();
		@Order(20)
		public final PM SATK = new PM();
		@Order(21)
		public final PM POIATK = new PM();
		@Order(22)
		public final PTM ARMOR = new PTM();
		@Order(23)
		public final SPEED SPEED = new SPEED();
		@Order(24)
		public final STRONG STRONG = new STRONG();
		@Order(25)
		public final PROB LETHAL = new PROB();
		@Order(26)
		public final IMU IMUKB = new IMU();
		@Order(27)
		public final IMU IMUSTOP = new IMU();
		@Order(28)
		public final IMU IMUSLOW = new IMU();
		@Order(29)
		public final IMU IMUWAVE = new IMU();
		@Order(30)
		public final IMU IMUVOLC = new IMU();
		@Order(31)
		public final IMUAD IMUWEAK = new IMUAD();
		@Order(32)
		public final IMU IMUWARP = new IMU();
		@Order(33)
		public final IMU IMUCURSE = new IMU();
		@Order(34)
		public final IMU IMUSEAL = new IMU();
		@Order(35)
		public final IMU IMUSUMMON = new IMU();
		@Order(36)
		public final IMUAD IMUPOI = new IMUAD();
		@Order(37)
		public final IMU IMUPOIATK = new IMU();
		@Order(38)
		public final MULT IMUMOVING = new MULT();
		@Order(39)
		public final CANNI IMUCANNON = new CANNI();
		@Order(40)
		public final IMUAD IMUARMOR = new IMUAD();
		@Order(41)
		public final IMUAD IMUSPEED = new IMUAD();
		@Order(42)
		public final IMU CRITI = new IMU();
		@Order(43)
		public final COUNTER COUNTER = new COUNTER();
		@Order(44)
		public final PT IMUATK = new PT();
		@Order(45)
		public final DMGCUT DMGCUT = new DMGCUT();
		@Order(46)
		public final DMGCAP DMGCAP = new DMGCAP();
		@Order(47)
		public final BURROW BURROW = new BURROW();
		@Order(48)
		public final REVIVE REVIVE = new REVIVE();
		@Order(49)
		public final BARRIER BARRIER = new BARRIER();
		@Order(50)
		public final DSHIELD DEMONSHIELD = new DSHIELD();
		@Order(51)
        public final VOLC DEATHSURGE = new VOLC();
		@Order(52)
		public final MULT BOUNTY = new MULT();
		@Order(53)
		public final MULT ATKBASE = new MULT();
		@Order(54)
		public final BSTHUNT BSTHUNT = new BSTHUNT();
		@Order(55)
		public final PM WORKERLV = new PM();
		@Order(56)
		public final CDSETTER CDSETTER = new CDSETTER();
		@Order(57)
		public final AURA WEAKAURA = new AURA();
		@Order(58)
		public final AURA STRONGAURA = new AURA();
		@Order(59)
		public final LETHARGY LETHARGY = new LETHARGY();
		@Order(60)
		public final IMUAD IMULETHARGY = new IMUAD();
		@Order(61)
		public final REMOTESHIELD REMOTESHIELD = new REMOTESHIELD();
		@Order(62)
		public final AI AI = new AI();
		@Order(63)
		public final PT RAGE = new PT();
		@Order(64)
		public final PT HYPNO = new PT();
		@Order(65)
		public final IMU IMURAGE = new IMU();
		@Order(66)
		public final IMU IMUHYPNO = new IMU();
		@Order(67)
		public final MINIVOLC MINIVOLC = new MINIVOLC();
		@Order(68)
		public final PM DEMONVOLC = new PM();
		@Order(69)
		public final MULT DMGINC = new MULT(); //Merges Strong against, Massive Damage, and Insane Damage
		@Order(70)
		public final MULT DEFINC = new MULT(); //Merges Strong against, Resistant, and Insane Resist
		@Order(71)
		public final RANGESHIELD RANGESHIELD = new RANGESHIELD();

		@Override
		public Proc clone() {
			try {
				Proc ans = new Proc();
				Field[] fs = getDeclaredFields();
				for (Field f : fs) {
					f.setAccessible(true);
					if(f.get(this) != null)
						f.set(ans, ((ProcItem) f.get(this)).clone());
					f.setAccessible(false);
				}
				return ans;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		public ProcItem get(String id) {
			try {
				return (ProcItem) Proc.class.getField(id).get(this);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		public ProcItem getArr(int i) {
			try {
				return (ProcItem) getDeclaredFields()[i].get(this);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		public boolean sharable(int i) {
			if(i >= procSharable.length) {
				System.out.println("Warning : "+i+" is out of index of procSharable");
				return false;
			} else {
				return procSharable[i];
			}
		}

		@Override
		public String toString() {
			return JsonEncoder.encode(this).toString();
		}

		/**
		 * Used to parse procs into pack data
		 * @return The encoded proc as json object
		 */
		@SuppressWarnings("unused")
		public JsonObject serProc() {
			JsonObject obj = new JsonObject();

			for(Field f : getDeclaredFields()) {
				try {
					String tag = f.getName();
					ProcItem proc = (ProcItem) f.get(this);

					if(proc.exists()) {
						obj.add(tag, JsonEncoder.encode(proc));
					}
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}

			return obj;
		}

		/**
		 * Used to generate procs from the pack's json data
		 * @param elem Json Component containing the proc's data
		 * @return The decoded proc
		 */
		@SuppressWarnings("unused")
		public static Proc genProc(JsonElement elem) {
			Proc proc = Proc.blank();
			if(elem == null)
				return proc;

			JsonObject obj = elem.getAsJsonObject();
			if(obj == null)
				return proc;

			for(Field f : getDeclaredFields()) {
				String tag = f.getName();
				try {
					if(obj.has(tag) && !obj.get(tag).isJsonNull()) {
						f.setAccessible(true);
						f.set(proc, JsonDecoder.decode(obj.get(tag), f.getType()));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			return proc;
		}

	}

	public static final byte restrict_name = 32;
	public static final byte SE_VICTORY = 8;
	public static final byte SE_DEFEAT = 9;
	public static final byte SE_HIT_0 = 20;
	public static final byte SE_HIT_1 = 21;
	public static final byte SE_DEATH_0 = 23;
	public static final byte SE_DEATH_1 = 24;
	public static final byte SE_HIT_BASE = 22;
	public static final byte SE_ZKILL = 59;
	public static final byte SE_CRIT = 44;
	public static final byte SE_SATK = 90;
	public static final byte SE_WAVE = 26;
	public static final byte SE_LETHAL = 50;
	public static final byte SE_P_WORKERLVUP = 53;
	public static final byte SE_P_WORKERLVDOWN = 107;
	public static final byte SE_P_RESEARCHUP = 18;
	public static final byte SE_P_RESEARCHDOWN = 36;
	public static final byte SE_WARP_ENTER = 73;
	public static final byte SE_WARP_EXIT = 74;
	public static final byte SE_BOSS = 45;
	public static final byte SE_SPEND_FAIL = 15;
	public static final byte SE_SPEND_SUC = 19;
	public static final byte SE_SPEND_REF = 27;
	public static final byte SE_RANGESHIELD = 17;
	public static final byte SE_CANNON_CHARGE = 28;
	public static final byte SE_BARRIER_ABI = 70;
	public static final byte SE_BARRIER_NON = 71;
	public static final byte SE_BARRIER_ATK = 72;
	public static final byte SE_POISON = 110;
	public static final byte SE_VOLC_START = 111;
	public static final byte SE_VOLC_LOOP = 112;
	public static final short SE_SHIELD_HIT = 136;
	public static final short SE_SHIELD_BROKEN = 139;
	public static final short SE_SHIELD_REGEN = 138;
	public static final short SE_SHIELD_BREAKER = 137;
	public static final short SE_DEATH_SURGE = 143;
	public static final short SE_COUNTER_SURGE = 159;

	public static final byte[][] SE_CANNON = { { 25, 26 }, { 60 }, { 61 }, { 36, 37 }, { 65, 83 }, { 84, 85 }, { 86 },
			{ 124 } };

	public static final short[] SE_ALL = { SE_VICTORY, SE_DEFEAT, SE_SPEND_FAIL, SE_RANGESHIELD, SE_SPEND_SUC, SE_HIT_0, SE_HIT_1, SE_HIT_BASE, SE_DEATH_0, SE_DEATH_1, 25, 26, SE_SPEND_REF,
			SE_CANNON_CHARGE, 37, 44, 45, 50, 59, 60, 61, 65, 73, 74, 83, 84, 85, 86, 90, SE_POISON, SE_VOLC_START, SE_VOLC_LOOP, 124, SE_SHIELD_HIT, SE_SHIELD_BREAKER, SE_SHIELD_REGEN,
			SE_SHIELD_BROKEN, SE_DEATH_SURGE, SE_COUNTER_SURGE };

	public static final byte RARITY_TOT = 6;

	// trait bit filter
	public static final byte TB_RED = 1;
	public static final byte TB_FLOAT = 2;
	public static final byte TB_BLACK = 4;
	public static final byte TB_METAL = 8;
	public static final byte TB_ANGEL = 16;
	public static final byte TB_ALIEN = 32;
	public static final byte TB_ZOMBIE = 64;
	public static final short TB_RELIC = 128;
	public static final short TB_WHITE = 256;
	public static final short TB_EVA = 512;
	public static final short TB_WITCH = 1024;
	public static final short TB_INFH = 2048;
	public static final short TB_DEMON = 4096, TB_DEMON_T = 2048;

	// trait index
	public static final byte TRAIT_RED = 0;
	public static final byte TRAIT_FLOAT = 1;
	public static final byte TRAIT_BLACK = 2;
	public static final byte TRAIT_METAL = 3;
	public static final byte TRAIT_ANGEL = 4;
	public static final byte TRAIT_ALIEN = 5;
	public static final byte TRAIT_ZOMBIE = 6;
	public static final byte TRAIT_DEMON = 7;
	public static final byte TRAIT_RELIC = 8;
	public static final byte TRAIT_WHITE = 9;
	public static final byte TRAIT_EVA = 10;
	public static final byte TRAIT_WITCH = 11;
	public static final byte TRAIT_BARON = 12;
	public static final byte TRAIT_BEAST = 13;
	public static final byte TRAIT_INFH = 14;
	public static final byte TRAIT_TOT = 15;

	// treasure
	public static final byte T_RED = 0;
	public static final byte T_FLOAT = 1;
	public static final byte T_BLACK = 2;
	public static final byte T_ANGEL = 3;
	public static final byte T_METAL = 4;
	public static final byte T_ALIEN = 5;
	public static final byte T_ZOMBIE = 6;

	// default tech value
	public static final int[] MLV = new int[] { 30, 30, 30, 30, 30, 30, 30, 10, 30 };

	// tech index
	public static final byte LV_RES = 0;
	public static final byte LV_ACC = 1;
	public static final byte LV_BASE = 2;
	public static final byte LV_WORK = 3;
	public static final byte LV_WALT = 4;
	public static final byte LV_RECH = 5;
	public static final byte LV_CATK = 6;
	public static final byte LV_CRG = 7;
	public static final int LV_XP = 8;
	public static final byte LV_TOT = 9;

	// default treasure value
	public static final int[] MT = new int[] { 300, 300, 300, 300, 300, 300, 600, 600, 600, 300, 300 };

	// treasure index
	public static final byte T_ATK = 0;
	public static final byte T_DEF = 1;
	public static final byte T_RES = 2;
	public static final byte T_ACC = 3;
	public static final byte T_WORK = 4;
	public static final byte T_WALT = 5;
	public static final byte T_RECH = 6;
	public static final byte T_CATK = 7;
	public static final byte T_BASE = 8;
	public static final byte T_XP1 = 9;
	public static final byte T_XP2 = 10;
	public static final byte T_TOT = 11;

	// abi bit filter
	public static final byte AB_ONLY = 1;
	public static final byte AB_METALIC = 1 << 1;
	public static final byte AB_SNIPERI = 1 << 2;
	public static final byte AB_TIMEI = 1 << 3;
	public static final byte AB_GHOST = 1 << 4;
	public static final byte AB_ZKILL = 1 << 5;
	public static final byte AB_WKILL = 1 << 6;
	public static final short AB_GLASS = 1 << 7;
	public static final short AB_THEMEI = 1 << 8;
	public static final short AB_EKILL = 1 << 9;
	public static final short AB_IMUSW = 1 << 10;
	public static final short AB_BAKILL = 1 << 11;
	public static final short AB_CKILL = 1 << 12;

	public static final byte ABI_ONLY = 0;
	public static final byte ABI_METALIC = 1;
	public static final byte ABI_SNIPERI = 2;
	public static final byte ABI_TIMEI = 3;
	public static final byte ABI_GHOST = 4;
	public static final byte ABI_ZKILL = 5;
	public static final byte ABI_WKILL = 6;
	public static final byte ABI_GLASS = 7;
	public static final byte ABI_THEMEI = 8;
	public static final byte ABI_EKILL = 9;
	public static final byte ABI_IMUSW = 10;
	public static final byte ABI_BAKILL = 11;
	public static final byte ABI_CKILL = 12;
	public static final byte ABI_TOT = 13;// 18 currently

	// proc index
	public static final byte P_KB = 0;
	public static final byte P_STOP = 1;
	public static final byte P_SLOW = 2;
	public static final byte P_CRIT = 3;
	public static final byte P_WAVE = 4;
	public static final byte P_MINIWAVE = 5;
	public static final byte P_MOVEWAVE = 6;
	public static final byte P_VOLC = 7;
	public static final byte P_WEAK = 8;
	public static final byte P_BREAK = 9;
	public static final byte P_SHIELDBREAK = 10;
	public static final byte P_WARP = 11;
	public static final byte P_CURSE = 12;
	public static final byte P_SEAL = 13;
	public static final byte P_SUMMON = 14;
	/**
	 * 0:prob, 1:speed, 2:width (left to right), 3:time, 4:origin (center), 5:itv
	 */
	public static final byte P_TIME = 15;
	public static final byte P_SNIPER = 16;
	/**
	 * 0:prob, 1:time (-1 means infinite), 2:ID, 3: type 0 : Change only BG 1 : Kill
	 * all and change BG
	 */
	public static final byte P_THEME = 17;
	public static final byte P_BOSS = 18;
	/**
	 * 0:prob, 1:time, 2:dmg, 3:itv, 4: conf +0: normal, +1: of total, +2: of
	 * current, +3: of lost, +4: unstackable
	 */
	public static final byte P_POISON = 19;
	public static final byte P_SATK = 20;
	/**
	 * official poison
	 */
	public static final byte P_POIATK = 21;
	/**
	 * Make target receive n% damage more/less 0: chance, 1: duration, 2: debuff
	 */
	public static final byte P_ARMOR = 22;
	/**
	 * Make target move faster/slower 0: chance, 1: duration, 2: speed, 3: type type
	 * 0: Current speed * (100 + n)% type 1: Current speed + n type 2: Fixed speed
	 */
	public static final byte P_SPEED = 23;
	public static final byte P_STRONG = 24;
	public static final byte P_LETHAL = 25;
	public static final byte P_IMUKB = 26;
	public static final byte P_IMUSTOP = 27;
	public static final byte P_IMUSLOW = 28;
	public static final byte P_IMUWAVE = 29;
	public static final byte P_IMUVOLC = 30;
	public static final byte P_IMUWEAK = 31;
	public static final byte P_IMUWARP = 32;
	public static final byte P_IMUCURSE = 33;
	public static final byte P_IMUSEAL = 34;
	public static final byte P_IMUSUMMON = 35;
	public static final byte P_IMUPOI = 36;
	public static final byte P_IMUPOIATK = 37;
	public static final byte P_IMUMOVING = 38;
	public static final byte P_IMUCANNON = 39;
	public static final byte P_IMUARMOR = 40;
	public static final byte P_IMUSPEED = 41;
	public static final byte P_CRITI = 42;
	public static final byte P_COUNTER = 43;
	public static final byte P_IMUATK = 44;
	public static final byte P_DMGCUT = 45;
	public static final byte P_DMGCAP = 46;
	public static final byte P_BURROW = 47;
	/**
	 * body proc: 0: add revive time for zombies, -1 to make it infinite, revivable
	 * zombies only 1: revive time 2: revive health 3: point 1 4: point 2 5: type:
	 * 0/1/2/3: duration: in range and normal/in range/ master lifetime/permanent
	 * +4: make Z-kill unusable +8: revive non-zombie also +16: applicapable to
	 * others
	 */
	public static final byte P_REVIVE = 48;
	public static final byte P_BARRIER = 49;
	public static final byte P_DEMONSHIELD = 50;
	public static final byte P_DEATHSURGE = 51;
	public static final byte P_BOUNTY = 52;
	public static final byte P_ATKBASE = 53;
	public static final byte P_BSTHUNT = 54; //Beast Killer
	public static final byte P_WORKERLV = 55;
	public static final byte P_CDSETTER = 56;
	public static final byte P_WEAKAURA = 57;
	public static final byte P_STRONGAURA = 58;
	public static final byte P_LETHARGY = 59;
	public static final byte P_IMULETHARGY = 60;
	public static final byte P_REMOTESHIELD = 61;
	public static final byte P_AI = 62;
	public static final byte P_RAGE = 63;
	public static final byte P_HYPNO = 64;
	public static final byte P_IMURAGE = 65;
	public static final byte P_IMUHYPNO = 66;
	public static final byte P_MINIVOLC = 67;
	public static final byte P_DEMONVOLC = 68;
	public static final byte P_DMGINC = 69; // nice
	public static final byte P_DEFINC = 70;
	public static final byte P_RANGESHIELD = 71;
	public static final byte PROC_TOT = 72;// 72

	public static final boolean[] procSharable = {
			false, //kb
			false, //freeze
			false, //slow
			false, //critical
			false, //wave
			false, //miniwave
			false, //move wave
			false, //volcano
			false, //weaken
			false, //barrier breaker
			false, //shield breaker
			false, //warp
			false, //curse
			false, //seal
			false, //summon
			false, //time
			false, //sniper
			false, //theme
			false, //boss wave
			false, //venom
			false, //savage blow
			false, //poison
			false, //armor
			false, //haste
			true,  //strengthen
			true,  //survive
			true,  //imu.kb
			true,  //imu.freeze
			true,  //imu.slow
			true,  //imu.wave
			true,  //imu.volcano
			true,  //imu.weaken
			true,  //imu.warp
			true,  //imu.curse
			true,  //imu.seal
			true,  //imu.summon
			true,  //imu.BCU poison
			true,  //imu.poison
			true,  //imu.moving atk
			true,  //imu.cannon
			true,  //imu.armor break
			true,  //imu.haste
			true,  //imu. critical
			true,  //invincibility
			true,  //damage cut
			true,  //damage cap
			true,  //counter
			true,  //burrow
			true,  //revive
			true,  //barrier
			true,  //demon barrier
			true,  //death surge
			false, //2x money
			false, //base destroyer
			true,  //beast hunter
			false, //Worker change
			false, //Cooldown change
			true,  //Weaken Aura
			true,  //Strengthen Aura
			false, //Lethargy
			true,  //Imu.Lethargy
			true,  //Remote shield
			true,  //AI
			false, //Rage
			false, //Hypno
			true,  //Imu. Rage
			true,  // Imu Hypno
			false, //Mini surge
			true,  //Counter Volc
			true,  //Massive DMG but good
			true,  //Resistant but good
			true   //Range Shield
	};

	/**
	 * Procs in here are shareable on any hit for BC entities, but not shareable for custom entities
	 */
	public static final int[] BCShareable = { P_BOUNTY, P_ATKBASE };

	public static final byte WT_WAVE = 1;
	public static final byte WT_MOVE = 2;
	public static final byte WT_CANN = 2;
	public static final byte WT_VOLC = 4;
	public static final byte WT_MINI = 8;
	public static final byte WT_MIVC = 16;
	public static final byte WT_MEGA = 32;
	public static final byte PC_P = 0, PC_AB = 1, PC_BASE = 2, PC_IMU = 3, PC_TRAIT = 4;
	public static final byte PC2_HP = 0;
	public static final byte PC2_ATK = 1;
	public static final byte PC2_SPEED = 2;
	public static final byte PC2_COST = 3;
	public static final byte PC2_CD = 4;
	public static final byte PC2_HB = 5;
	public static final byte PC2_TBA = 6;
	public static final byte PC2_RNG = 7;
	public static final byte PC2_TOT = 8;
	// -1 for None
	// 0 for Proc
	// 1 for Ability
	// 2 for Base stat
	// 3 for Immune
	// 4 for Trait
	// 5 for special cases
	public static final int[][] PC_CORRES = new int[][] { // NP value table
			{ -1, 0 }, // 0:
			{ 0, P_WEAK }, // 1: weak, reversed health or relic-weak
			{ 0, P_STOP }, // 2: stop
			{ 0, P_SLOW }, // 3: slow
			{ 1, AB_ONLY }, // 4:Target Obnly
			{ 5, P_DMGINC, 150 }, // 5:Strong Vs.
			{ 5, P_DEFINC, 400 }, // 6:Resistant
			{ 5, P_DMGINC, 300 }, // 7:Massive Dmg
			{ 0, P_KB }, // 8: kb
			{ 0, P_WARP }, // 9:Warp
			{ 0, P_STRONG }, // 10: berserker, reversed health
			{ 0, P_LETHAL }, // 11: lethal
			{ 0, P_ATKBASE }, // 12: Base Destroyer
			{ 0, P_CRIT }, // 13: crit
			{ 1, AB_ZKILL }, // 14: zkill
			{ 0, P_BREAK }, // 15: break
			{ 0, P_BOUNTY }, // 16: 2x income
			{ 0, P_WAVE }, // 17: wave
			{ 0, P_IMUWEAK }, // 18: res weak
			{ 0, P_IMUSTOP }, // 19: res stop
			{ 0, P_IMUSLOW }, // 20: res slow
			{ 0, P_IMUKB }, // 21: res kb
			{ 0, P_IMUWAVE }, // 22: res wave
			{ 5, P_IMUWAVE }, // 23: waveblock
			{ 0, P_IMUWARP }, // 24: res warp
			{ 2, PC2_COST }, // 25: reduce cost
			{ 2, PC2_CD }, // 26: reduce cooldown
			{ 2, PC2_SPEED }, // 27: inc speed
			{ 2, PC2_HB }, // 28: inc knockbacks
			{ 3, P_IMUCURSE }, // 29: imu curse
			{ 0, P_IMUCURSE }, // 30: res curse
			{ 2, PC2_ATK }, // 31: inc ATK
			{ 2, PC2_HP }, // 32: inc HP
			{ 4, TRAIT_RED }, // 33: target red
			{ 4, TRAIT_FLOAT }, // 34: target floating
			{ 4, TRAIT_BLACK }, // 35: target black
			{ 4, TRAIT_METAL }, // 36: target metal
			{ 4, TRAIT_ANGEL }, // 37: target angel
			{ 4, TRAIT_ALIEN }, // 38: target alien
			{ 4, TRAIT_ZOMBIE }, // 39: target zombie
			{ 4, TRAIT_RELIC }, // 40: target relic
			{ 4, TRAIT_WHITE }, // 41: target white
			{ 4, TRAIT_WITCH }, // 42: target witch
			{ 4, TRAIT_EVA }, // 43: target EVA
			{ 3, P_IMUWEAK }, // 44: immune to weak
			{ 3, P_IMUSTOP }, // 45: immune to freeze
			{ 3, P_IMUSLOW }, // 46: immune to slow
			{ 3, P_IMUKB }, // 47: immune to kb
			{ 3, P_IMUWAVE }, // 48: immune to wave
			{ 3, P_IMUWARP }, // 49: immune to warp
			{ 0, P_SATK }, // 50: savage blow
			{ 0, P_IMUATK }, // 51: immune to attack
			{ 0, P_IMUPOIATK }, // 52: resist to poison ?
			{ 3, P_IMUPOIATK }, // 53: immune to poison
			{ 0, P_IMUVOLC }, // 54: resist to surge ?
			{ 3, P_IMUVOLC }, // 55: immune to surge
			{ 0, P_VOLC }, // 56: surge, level up to chance up
			{ 4, TRAIT_DEMON }, // 57: Targetting Aku
			{ 0, P_SHIELDBREAK }, //58 : shield piercing
			{ 1, AB_CKILL }, //59 : corpse killer
			{ 0, P_CURSE }, //60 : curse
			{ 2, PC2_TBA }, //61 : tba
			{ 0, P_MINIWAVE }, //62 : mini-wave
			{ 1, AB_BAKILL }, //63 : baron killer
			{ 0, P_BSTHUNT, 1 }, //64 : behemoth slayer
			{ 0, P_MINIVOLC } //65 : MiniSurge
	};
	public static final int[][] PC_CUSTOM = new int[][] { //Use negative ints to handle (it would be so awesome, it would be so cool)
			{ -1, 0 }, // 0:
			{ 0, P_BURROW}, // 1: Burrow
			{ 0, P_REVIVE}, //2: Revive
			{ 0, P_BARRIER}, //3: Barrier
			{ 0, P_DEMONSHIELD}, //4: Aku Shield
			{ 0, P_DEATHSURGE}, //5: Death Surge
			{ 0, P_DEMONVOLC}, //6: Surge Counter
			{ 0, P_SEAL}, // 7: Seal
			{ 0, P_COUNTER}, // 8: Counter
			{ 0, P_DMGCUT}, // 9: Super Armor
			{ 0, P_DMGCAP}, // 10: Mystic Shield
			{ 0, P_REMOTESHIELD}, // 11: Remote Shield
			{ 0, P_ARMOR}, // 12: Armor break
			{ 0, P_SPEED}, // 13: Haste
			{ 0, P_RAGE}, // 14: Rage
			{ 0, P_HYPNO}, // 15: Hypno
			{ 0, P_CRITI}, // 16: Criti
			{ 0, P_IMUSUMMON}, // 17: Summon immune
			{ 0, P_IMUSEAL}, // 18: Seal immune
			{ 0, P_IMUARMOR}, // 19: Armor Break immune
			{ 0, P_IMUSPEED}, // 20: Haste immune
			{ 0, P_IMULETHARGY}, // 21: Lethargy Immunity
			{ 0, P_IMURAGE}, // 22: Rage Immunity
			{ 0, P_IMUHYPNO}, // 23: Hypno Immunity
			{ 2, PC2_RNG}, // 24: Range
			{ 0, P_POIATK}, // 25: Toxic
			{ 0, P_IMUPOI}, // 26: Imu. BCU Poison
			{ 1, AB_SNIPERI}, // 27: IMU.Sniper
			{ 1, AB_TIMEI}, // 28: IMU.TimeStop
			{ 1, AB_THEMEI}, // 29: IMU.Theme
			{ 1, AB_IMUSW}, // 30: IMU.BossWave
			{ 0, P_LETHARGY}, // 31: Lethargy
			{ 0, P_SNIPER}, // 32: Sniper KB
			{ 0, P_BOSS}, // 33: Bosswave
			{ 0, P_TIME}, // 34: Timestop
			{ 0, P_IMUMOVING}, // 35: IMU.MoveATK
			{ 0, P_WEAKAURA}, // 36: WeakenAura
			{ 0, P_STRONGAURA}, // 37: StrengthAura
			{ 0, P_DMGINC}, // 38: ExtraDmg
			{ 0, P_DEFINC},  // 39: Resistance
			{ 0, P_RANGESHIELD} //40: Range Shield
	};

	public static int[] get_CORRES(int ind) {
		if (ind < 0)
			return PC_CUSTOM[Math.abs(ind)];

		if (ind >= PC_CORRES.length)
			return PC_CORRES[0];
		return PC_CORRES[ind];
	}

	// foot icon index used in battle
	public static final byte INV = -1;
	public static final byte INVWARP = -2;
	public static final byte STPWAVE = -3;
	public static final byte BREAK_ABI = -4;
	public static final byte BREAK_ATK = -5;
	public static final byte BREAK_NON = -6;
	public static final byte HEAL = -7;
	public static final byte SHIELD_HIT = -8;
	public static final byte SHIELD_BROKEN = -9;
	public static final byte SHIELD_REGEN = -10;
	public static final byte SHIELD_BREAKER = -11;
	public static final byte DMGCAP_FAIL = -12;
	public static final byte DMGCAP_SUCCESS = -13;
	public static final byte REMSHIELD_NEAR = -14;
	public static final byte REMSHIELD_FAR = -15;
	public static final byte A_WEAKAURASTR = -16;
	public static final byte A_STRAURAWEAK = -17;
	public static final byte RANGESHIELD_SINGLE = -18;

	// Combo index
	public static final byte C_ATK = 0;
	public static final byte C_DEF = 1;
	public static final byte C_SPE = 2;
	public static final byte C_GOOD = 14;
	public static final byte C_MASSIVE = 15;
	public static final byte C_RESIST = 16;
	public static final byte C_KB = 17;
	public static final byte C_SLOW = 18;
	public static final byte C_STOP = 19;
	public static final byte C_WEAK = 20;
	public static final byte C_STRONG = 21;
	public static final byte C_WKILL = 22;
	public static final byte C_EKILL = 23;
	public static final byte C_CRIT = 24;
	public static final byte C_C_INI = 3;
	public static final byte C_C_ATK = 6;
	public static final byte C_C_SPE = 7;
	public static final byte C_BASE = 10;
	public static final byte C_M_INI = 5;
	public static final byte C_M_LV = 4;
	public static final byte C_M_INC = 8;
	public static final byte C_M_MAX = 9;
	public static final byte C_RESP = 11;
	public static final byte C_MEAR = 12;
	public static final byte C_TOT = 25;

	// Effects Anim index
	public static final byte A_DOWN = 0;
	public static final byte A_UP = 1;
	public static final byte A_SLOW = 2;
	public static final byte A_STOP = 3;
	public static final byte A_CURSE = 4;
	public static final byte A_SHIELD = 5;
	public static final byte A_FARATTACK = 6;
	public static final byte A_WAVE_INVALID = 7;
	public static final byte A_WAVE_STOP = 8;
	public static final byte A_EFF_INV = 9;
	public static final byte A_B = 10;
	public static final byte A_SEAL = 11;
	public static final byte A_POI0 = 12;
	public static final byte A_POI1 = 13;
	public static final byte A_POI2 = 14;
	public static final byte A_POI3 = 15;
	public static final byte A_POI4 = 16;
	public static final byte A_POI5 = 17;
	public static final byte A_POI6 = 18;
	public static final byte A_POI7 = 19;
	public static final byte[] A_POIS = { A_POI0, A_POI1, A_POI2, A_POI3, A_POI4, A_POI5, A_POI6, A_POI7 };
	public static final byte A_IMUATK = 20;
	public static final byte A_ARMOR = 21;
	public static final byte A_SPEED = 22;
	public static final byte A_WEAK_UP = 23;
	public static final byte A_HEAL = 24;
	public static final byte A_DEMON_SHIELD = 25;
	public static final byte A_COUNTER = 26;
	public static final byte A_DMGCUT = 27;
	public static final byte A_DMGCAP = 28;
	public static final byte A_LETHARGY = 29;
	public static final byte A_REMSHIELD = 30;
	public static final byte A_WEAKAURA = 31;
	public static final byte A_STRONGAURA = 32;
	public static final byte A_RAGE = 33;
	public static final byte A_HYPNO = 34;
	public static final byte A_RANGESHIELD = 35;
	public static final byte A_TOT = 36;

	// atk type index used in filter page
	public static final byte ATK_SINGLE = 0;
	public static final byte ATK_AREA = 1;
	public static final byte ATK_LD = 2;
	public static final byte ATK_OMNI = 4;
	public static final byte ATK_TOT = 12;

	// base and canon level
	public static final byte BASE_H = 0;
	public static final byte BASE_SLOW = 1;
	public static final byte BASE_WALL = 2;
	public static final byte BASE_STOP = 3;
	public static final byte BASE_WATER = 4;
	public static final byte BASE_GROUND = 5;
	public static final byte BASE_BARRIER = 6;
	public static final byte BASE_CURSE = 7;
	public static final byte BASE_TOT = 8;

	// base type
	public static final byte BASE_ATK_MAGNIFICATION = 0;
	public static final byte BASE_SLOW_TIME = 1;
	public static final byte BASE_TIME = 2;
	public static final byte BASE_WALL_MAGNIFICATION = 3;
	public static final byte BASE_WALL_ALIVE_TIME = 4;
	public static final byte BASE_RANGE = 5;
	//Figure out type 6
	public static final byte BASE_HEALTH_PERCENTAGE = 7;
	//Figure out type 8
	public static final byte BASE_HOLY_ATK_SURFACE = 9;
	public static final byte BASE_HOLY_ATK_UNDERGROUND = 10;
	//Figure out type 11
	public static final byte BASE_CURSE_TIME = 12;

	// decoration/base level
	public static final byte DECO_BASE_SLOW = 1;
	public static final byte DECO_BASE_WALL = 2;
	public static final byte DECO_BASE_STOP = 3;
	public static final byte DECO_BASE_WATER = 4;
	public static final byte DECO_BASE_GROUND = 5;
	public static final byte DECO_BASE_BARRIER = 6;
	public static final byte DECO_BASE_CURSE = 7;
	public static final byte DECO_BASE_TOT = 7;

	public static final byte[] DECOS = new byte[]{P_SLOW, -1, P_STOP, -1, P_WEAK, P_POIATK, P_CURSE}; //-1s are wave and surge (in that order)

	// touchable ID
	public static final byte TCH_N = 1;
	public static final byte TCH_KB = 2;
	public static final byte TCH_UG = 4;
	public static final byte TCH_CORPSE = 8;
	public static final byte TCH_SOUL = 16;
	public static final byte TCH_EX = 32;
	public static final byte TCH_ZOMBX = 64;
	public static final short TCH_ENTER = 128;

	public static final String[] A_PATH = new String[] { "down", "up", "slow", "stop", "shield", "farattack",
			"wave_invalid", "wave_stop", "waveguard" };

	// After this line all number is game data

	public static final byte INT_KB = 0, INT_HB = 1, INT_SW = 2, INT_ASS = 3, INT_WARP = 4;

	public static final byte[] KB_PRI = new byte[] { 2, 4, 5, 1, 3 };
	public static final byte[] KB_TIME = new byte[] { 11, 23, 47, 11, -1 };
	public static final short[] KB_DIS = new short[] { 165, 345, 705, 55, -1 };

	public static final float W_E_INI = -32.75f;
	public static final float W_U_INI = -67.5f;
	public static final short W_PROG = 200;
	public static final short W_E_WID = 500;
	public static final short W_U_WID = 400;
	public static final byte W_TIME = 3;
	public static final byte W_MINI_TIME = 1; // mini wave spawn interval
	public static final byte W_MEGA_TIME = 6;
	public static final short W_VOLC_INNER = 250; // volcano inner width
	public static final byte W_VOLC_PIERCE = 125; // volcano pierce width
	public static final byte VOLC_ITV = 20;

	public static final byte VOLC_PRE = 15; // volcano pre-atk
	public static final byte VOLC_POST = 10; // volcano post-atk
	public static final byte VOLC_SE = 30; // volcano se loop duration

	public static final byte[] NYPRE = new byte[] { 18, 2, -1, 28, 37, 18, 10, 2 };// not sure, 10f for bblast
	public static final short[] NYRAN = new short[] { 710, 600, -1, 500, 500, 710, 100, 600 };// not sure
	public static final short SNIPER_CD = 300;// not sure
	public static final byte SNIPER_PRE = 10;// not sure
	public static final float SNIPER_POS = 442.5f;
	public static final byte REVIVE_SHOW_TIME = 16;

	public static final int ORB_ATK = 0;
	public static final int ORB_RES = 1;
	public static final int ORB_STRONG = 2;
	public static final int ORB_MASSIVE = 3;
	public static final int ORB_RESISTANT = 4;
	public static final int ORB_TYPE = 0, ORB_TRAIT = 1, ORB_GRADE = 2, ORB_TOT = 3;

	public static final short[] ORB_ATK_MULTI = { 100, 200, 300, 400, 500 }; // Atk orb multiplication
	public static final byte[] ORB_RES_MULTI = { 4, 8, 12, 16, 20 }; // Resist orb multiplication
	public static final byte[] ORB_STR_DEF_MULTI = {2, 4, 6, 8, 10};
	public static final float[] ORB_STR_ATK_MULTI = {0.06f, 0.12f, 0.18f, 0.24f, 0.3f};
	public static final float[] ORB_MASSIVE_MULTI = {0.1f, 0.2f, 0.3f, 0.4f, 0.5f};
	public static final byte[] ORB_RESISTANT_MULTI = {5, 10, 15, 20, 25};
	public static final short[] GATYA = { 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 160, 161, 164, 167,
			168, 169, 170, 171, 179, 180, 181, 182, 183, 184};

	public static final short MUSIC_DELAY = 2344; //Music change delay with milliseconds accuracy

	public static final byte LINEUP_CHANGE_TIME = 6; //in frame

	public static final byte BG_EFFECT_STAR = 0;
	public static final byte BG_EFFECT_RAIN = 1;
	public static final byte BG_EFFECT_BUBBLE = 2;
	public static final byte BG_EFFECT_FALLING_SNOW = 3;
	public static final byte BG_EFFECT_SNOW = 4;
	public static final byte BG_EFFECT_SNOWSTAR = 5;
	public static final byte BG_EFFECT_BLIZZARD = 6;
	public static final byte BG_EFFECT_SHINING = 7;
	public static final byte BG_EFFECT_BALLOON = 8;
	public static final byte BG_EFFECT_ROCK = 9;

	//Below are completely guessed
	public static final int BG_EFFECT_STAR_TIME = 35;
	public static final short BG_EFFECT_STAR_Y_RANGE = 140;
	public static final byte BG_EFFECT_SPLASH_MIN_HEIGHT = 90;
	public static final byte BG_EFFECT_SPLASH_RANGE = 60;
	public static final short BG_EFFECT_BUBBLE_TIME = 780;
	public static final byte BG_EFFECT_BUBBLE_FACTOR = 32;
	public static final byte BG_EFFECT_BUBBLE_STABILIZER = 7;
	public static final byte BG_EFFECT_SNOW_SPEED = 8;
	public static final float[] BG_EFFECT_BLIZZARD_SIZE = {1.0f, 1.5f, 2.0f};
	public static final float BG_EFFECT_BLIZZARD_SPEED = 40;
	public static final byte BG_EFFECT_FALLING_SNOW_SPEED = 3;
	public static final float BG_EFFECT_FALLING_SNOW_SIZE = 2.0f;
	public static final byte BG_EFFECT_SHINING_TIME = 8;
	public static final float BG_EFFECT_BALLOON_SPEED = 1f;
	public static final byte BG_EFFECT_BALLOON_FACTOR = 32;
	public static final byte BG_EFFECT_BALLOON_STABILIZER = 25;
	public static final float[] BG_EFFECT_ROCK_SIZE = {1.0f, 2.25f};
	public static final byte[] BG_EFFECT_ROCK_SPEED = {1, 3};
	public static final short BG_EFFECT_ROCK_BEHIND_SPAWN_OFFSET = 190;

	public static final byte[] SHAKE_MODE_HIT = {5, 7, 2, 30};
	public static final byte[] SHAKE_MODE_BOSS = {10, 15, 2, 0};
	public static final byte SHAKE_DURATION = 0;
	public static final byte SHAKE_INITIAL = 1;
	public static final byte SHAKE_END = 2;
	public static final byte SHAKE_COOL_DOWN = 3;
	public static final float SHAKE_STABILIZER = 2.5f;
	public static final int COUNTER_SURGE_FORESWING = 50;
	public static final int COUNTER_SURGE_SOUND = 18;

	public static final char[] SUFX = new char[]{'f', 'c', 's'};

	public static EffAnim.EffAnimStore effas() {
		return CommonStatic.getBCAssets().effas;
	}

	/**
	 * convenient method to log an unexpected error. Don't use it to process any
	 * expected error
	 */
	public static boolean err(RunExc s) {
		return CommonStatic.ctx.noticeErr(s, ErrType.ERROR, "unexpected error");
	}

	/**
	 * convenient method to log an unexpected error. Don't use it to process any
	 * expected error.
	 */
	public static <T> T err(SupExc<T> s) {
		try {
			return s.get();
		} catch (Exception e) {
			CommonStatic.ctx.noticeErr(e, ErrType.ERROR, "Unexpected Error: " + e + " in " + e.getStackTrace()[0].toString());
			return null;
		}
	}

	public static int getVer(String ver) {
		int ans = 0;
		int[] strs = CommonStatic.parseIntsN(ver);
		for (int str : strs) {
			ans *= 100;
			ans += str;
		}
		return ans;
	}

	public static String hex(int id) {
		return trio(id / 1000) + trio(id % 1000);
	}

	public static <T> T ignore(SupExc<T> sup) {
		try {
			return sup.get();
		} catch (Exception e) {
			return null;
		}
	}

	public static String restrict(String str) {
		if (str.length() < restrict_name)
			return str;
		return str.substring(0, restrict_name);
	}

	public static String revVer(int ver) {
		return ver / 1000000 % 100 + "-" + ver / 10000 % 100 + "-" + ver / 100 % 100 + "-" + ver % 100;
	}

	public static String duo(int i) {
		if(i < 10) {
			return "0"+ i;
		} else {
			return "" + i;
		}
	}

	public static String trio(int i) {
		if(i < 10)
			return "00" + i;
		else if(i < 100)
			return "0" + i;
		else
			return "" + i;
	}
}
