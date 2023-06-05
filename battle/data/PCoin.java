package common.battle.data;

import common.CommonStatic;
import common.io.json.JsonClass;
import common.io.json.JsonDecoder.OnInjected;
import common.io.json.JsonField;
import common.pack.Context.ErrType;
import common.pack.Identifier;
import common.pack.SortedPackSet;
import common.pack.UserProfile;
import common.system.files.VFile;
import common.util.Data;
import common.util.Data.Proc.ProcItem;
import common.util.unit.Trait;
import common.util.unit.Unit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;

@JsonClass(read = JsonClass.RType.FILL)
public class PCoin extends Data {
	public static void read() {
		Queue<String> qs = VFile.readLine("./org/data/SkillAcquisition.csv");

		qs.poll();

		for (String str : qs) {
			String[] strs = str.trim().split(",");

			if (strs.length == 114)
				new PCoin(strs);
		}
	}

	private final MaskUnit du;
	public MaskUnit full = null;
	public SortedPackSet<Trait> trait = new SortedPackSet<>();

	@JsonField(block = true)
	public int[] max;
	@JsonField(generic = int[].class)
	public final ArrayList<int[]> info = new ArrayList<>();

	public PCoin(CustomEntity ce) {
		du = (CustomUnit)ce;
		((CustomUnit)du).pcoin = this;
	}

	private PCoin(String[] strs) {
		du = Identifier.parseInt(CommonStatic.parseIntN(strs[0]), Unit.class).get().getForms()[2].du;
		trait = Trait.convertType(CommonStatic.parseIntN(strs[1]));

		for (int i = 0; i < 8; i++)
			if(!strs[2 + i * 14].equals("0")) {
				int[] data = new int[14]; //Default length of BC
				for (int j = 0; j < 14; j++)
					data[j] = CommonStatic.parseIntN(strs[2 + i * 14 + j]);
				if (data[13] == 1) //Super Talent
					data[13] = 60;
				if(data[0] == 62) //Miniwave
					if(data[6] == 0 && data[7] == 0) {
						data[6] = 20;
						data[7] = 20;
					}

				int[] corres = Data.get_CORRES(data[0]);
				if (corres[0] == -1) {
					CommonStatic.ctx.printErr(ErrType.WARN, "new PCoin ability for " + du.getPack() + " not yet handled by BCU: " + data[0] + "\nData is " + Arrays.toString(data));
					continue;
				}
				int[] trueArr;
				switch (corres[0]) {
					case Data.PC_P:
						trueArr = Arrays.copyOf(data, 3 + (du.getProc().getArr(corres[1]).getDeclaredFields().length - (corres.length >= 3 ? corres[2] : 0)) * 2); //The Math.min is for testing
						break;
					case Data.PC_BASE:
						trueArr = Arrays.copyOf(data, 4);
						break;
					default:
						trueArr = Arrays.copyOf(data, 3);
				}
				trueArr[trueArr.length - 1] = Math.max(0, data[13]);
				info.add(trueArr);
			}
		max = new int[info.size()];
		for (int i = 0; i < info.size(); i++)
			max[i] = Math.max(1, info.get(i)[1]);
		((DataUnit)du).pcoin = this;

		full = improve(max);
	}

	public void update() {
		full = improve(max);
	}

	@SuppressWarnings("deprecation")
	public MaskUnit improve(int[] talents) {
		MaskUnit ans = du.clone();

		int[] temp;

		if (talents.length < max.length) {
			temp = new int[max.length];
			System.arraycopy(talents, 0, temp, 0, talents.length);
			System.arraycopy(max, talents.length, temp, talents.length, max.length - talents.length);
		} else
			temp = talents.clone();

		talents = temp;
		for (int i = 0; i < info.size(); i++) {
			int[] type = get_CORRES(info.get(i)[0]);

			if (talents[i] == 0)
				continue;

			//Targettings that come with a talent, such as Hyper Mr's
			if (this.trait.size() > 0)
				if (!ans.getTraits().contains(this.trait.get(0)))
					ans.getTraits().add(this.trait.get(0));

			int offset = type.length >= 3 ? type[2] : 0;
			int fieldTOT = -offset;
			if (type[0] == PC_P)
				fieldTOT += ans.getProc().getArr(type[1]).getDeclaredFields().length; //The Math.min is for testing
			else if (type[0] == PC_BASE)
				fieldTOT = 1;

			int maxlv = info.get(i)[1];
			int[] modifs = new int[fieldTOT];

			if (maxlv > 1) {
				for (int j = 0; j < fieldTOT; j++) {
					int v0 = info.get(i)[2 + j * 2];
					int v1 = info.get(i)[3 + j * 2];
					modifs[j] = (v1 - v0) * (talents[i] - 1) / (maxlv - 1) + v0;
				}
			} else
				for (int j = 0; j < fieldTOT; j++)
					modifs[j] = info.get(i)[3 + j * 2];

			if (type[0] == PC_P) {
				ProcItem tar = ans.getProc().getArr(type[1]);

				if (type[1] == P_VOLC || type[1] == P_MINIVOLC) {
					if (du instanceof DataUnit) {
						tar.set(0, modifs[0]);
						tar.set(1, modifs[2] / 4);
						tar.set(2, (modifs[2] + modifs[3]) / 4);
						tar.set(3, modifs[1] * 20);
					} else {
						tar.set(0, modifs[0]);
						tar.set(1, Math.min(modifs[1], modifs[2]));
						tar.set(2, Math.max(modifs[1], modifs[2]));
						tar.set(3, modifs[3]);
					}
					if (type[1] == P_MINIVOLC && tar.get(4) == 0)
						tar.set(4, 20);
				} else
					for (int j = 0; j < fieldTOT; j++)
						if (modifs[j] > 0)
							tar.set(j + offset, tar.get(j + offset) + modifs[j]);
				if (type[1] == P_BSTHUNT)
					ans.getProc().BSTHUNT.type.active = modifs[0] > 0;

				if (du instanceof DataUnit) {
					if (type[1] == P_STRONG && modifs[0] != 0)
						tar.set(0, 100 - tar.get(0));
					else if (type[1] == P_WEAK)
						tar.set(2, 100 - tar.get(2));
					else if (type[1] == P_BOUNTY)
						tar.set(0, 100);
					else if (type[1] == P_ATKBASE)
						tar.set(0, 300);
				} else if (!((CustomEntity)du).common && !(type[1] == P_STRONG && modifs[0] != 0)) {
					for (AtkDataModel[] atkss : ((CustomEntity)ans).hits) {
						for (AtkDataModel atk : atkss) {
							ProcItem atks = atk.proc.getArr(type[1]);

							if (type[1] == P_VOLC) {
								atks.set(0, modifs[0]);
								atks.set(1, Math.min(modifs[1], modifs[2]));
								atks.set(2, Math.max(modifs[1], modifs[2]));
								atks.set(3, modifs[3]);
							} else
								for (int j = 0; j < fieldTOT; j++)
									if (modifs[j] > 0)
										atks.set(j, atks.get(j) + modifs[j]);
						}
					}
					for (AtkDataModel[] atks : ans.getSpAtks(true))
						for (AtkDataModel atk : atks) {
							ProcItem atkp = atk.proc.getArr(type[1]);

							if (type[1] == P_VOLC) {
								atkp.set(0, modifs[0]);
								atkp.set(1, Math.min(modifs[1], modifs[2]));
								atkp.set(2, Math.max(modifs[1], modifs[2]));
								atkp.set(3, modifs[3]);
							} else
								for (int j = 0; j < fieldTOT; j++)
									if (modifs[j] > 0)
										atkp.set(j, atkp.get(j) + modifs[j]);
						}
				}
			} else if (type[0] == PC_AB || type[0] == PC_BASE)
				ans.improve(type, type[0] == PC_BASE ? modifs[0] : 0);
			else if (type[0] == PC_IMU)
				ans.getProc().getArr(type[1]).set(0, 100);
			else if (type[0] == PC_TRAIT) {
				Trait types = UserProfile.getBCData().traits.get(type[1]);

				if (!ans.getTraits().contains(types))
					ans.getTraits().add(types);
			} else if (type[0] == 5) //waveblock
				ans.getProc().getArr(type[1]).set(1, 100);
		}

		return ans;
	}

	public double getStatMultiplication(int mult, int[] talents) {
		for(int i = 0; i < info.size(); i++) {
			if(talents[i] == 0 || info.get(i)[0] >= PC_CORRES.length || info.get(i)[0] < 0)
				continue;

			int[] type = PC_CORRES[info.get(i)[0]];
			if(type[0] == PC_BASE && type[1] == mult) {
				int maxlv = info.get(i)[1];
				if (maxlv > 1) {
					int v0 = info.get(i)[2];
					int v1 = info.get(i)[3];
					int modif = (v1 - v0) * (talents[i] - 1) / (maxlv - 1) + v0;
					return 1 + modif * 0.01;
				}
				return 1 + info.get(i)[3] * 0.01;
			}
		}
		return 1.0;
	}

	public int getReqLv(int i) {
		int[] tal = info.get(i);
		return tal[tal.length - 1];
	}

	@OnInjected
	public void onInjected() {
		max = new int[info.size()];
		for (int i = 0; i < info.size(); i++)
			max[i] = Math.max(1, info.get(i)[1]);
	}
}
