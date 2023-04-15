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
		int id = CommonStatic.parseIntN(strs[0]);
		trait = Trait.convertType(CommonStatic.parseIntN(strs[1]));

		for (int i = 0; i < 8; i++)
			if(!strs[2 + i * 14].equals("0")) {
				int[] data = new int[14];

				for (int j = 0; j < 14; j++)
					data[j] = CommonStatic.parseIntN(strs[2 + i * 14 + j]);
				if (data[13] == 1) //Super Talent
					data[13] = 60;

				if(data[0] == 62) //Miniwave
					if(data[6] == 0 && data[7] == 0) {
						data[6] = 20;
						data[7] = 20;
					}
				info.add(data);
			}
		max = new int[info.size()];
		for (int i = 0; i < info.size(); i++)
			max[i] = Math.max(1, info.get(i)[1]);
		du = Identifier.parseInt(id, Unit.class).get().getForms()[2].du;
		((DataUnit)du).pcoin = this;

		full = improve(max);
	}

	public void update() {
		// Apparently, if max is null, since we will update full var anyway
		// we can just re-generate whole array
		if (max == null || max.length < info.size()) {
			max = new int[info.size()];

			for (int i = 0; i < info.size(); i++) {
				max[i] = Math.max(1, info.get(i)[1]);
			}
		}

		full = improve(max);
	}

	@SuppressWarnings("deprecation")
	public MaskUnit improve(int[] talents) {
		MaskUnit ans = du.clone();

		int[] temp;

		if (talents.length < max.length) {
			temp = new int[max.length];

			System.arraycopy(talents, 0, temp, 0, talents.length);

			if (max.length > talents.length)
				System.arraycopy(max, talents.length, temp, talents.length, max.length - talents.length);
		} else {
			temp = talents.clone();
		}

		talents = temp;

		for (int i = 0; i < info.size(); i++) {
			if (info.get(i)[0] >= PC_CORRES.length) {
				CommonStatic.ctx.printErr(ErrType.NEW, "new PCoin ability not yet handled by BCU: " + info.get(i)[0] + "\nText ID is " + info.get(i)[10]+"\nData is "+Arrays.toString(info.get(i)));
				continue;
			}

			int[] type = PC_CORRES[info.get(i)[0]];

			if (type[0] == -1) {
				CommonStatic.ctx.printErr(ErrType.NEW, "new PCoin ability not yet handled by BCU: " + info.get(i)[0] + "\nText ID is " + info.get(i)[10]+"\nData is "+Arrays.toString(info.get(i)));
				continue;
			}

			if (talents[i] == 0) {
				if (type[0] == PC_TRAIT) {
					Trait types = UserProfile.getBCData().traits.get(type[1]);
					ans.getTraits().remove(types);
				}
				continue;
			}

			//Targettings that come with a talent, such as Hyper Mr's
			if (this.trait.size() > 0)
				if (!ans.getTraits().contains(this.trait.get(0)))
					ans.getTraits().add(this.trait.get(0));

			int maxlv = info.get(i)[1];

			int[] modifs = new int[4];

			if (maxlv > 1) {
				for (int j = 0; j < 4; j++) {
					int v0 = info.get(i)[2 + j * 2];
					int v1 = info.get(i)[3 + j * 2];
					modifs[j] = (v1 - v0) * (talents[i] - 1) / (maxlv - 1) + v0;
				}
			} else
				for (int j = 0; j < 4; j++)
					modifs[j] = info.get(i)[3 + j * 2];

			if (type[0] == PC_P) {
				ProcItem tar = ans.getProc().getArr(type[1]);
				int offset = type.length >= 3 ? type[2] : 0;

				if (type[1] == P_VOLC) {
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
				} else
					for (int j = 0; j < 4 - offset; j++)
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
								for (int j = 0; j < 4; j++)
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
								for (int j = 0; j < 4; j++)
									if (modifs[j] > 0)
										atkp.set(j, atkp.get(j) + modifs[j]);
						}
				}
			} else if (type[0] == PC_AB || type[0] == PC_BASE) {
				if (du instanceof DataUnit)
					improve((DataUnit)ans,type,modifs);
				else
					improve((CustomUnit)ans,type,modifs);
			} else if (type[0] == PC_IMU)
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

	private void improve(DataUnit ans, int[] type, int[] modifs) {
		if (type[0] == PC_AB)
			ans.abi |= type[1];
		else {
			switch (type[1]) {
				case PC2_SPEED:
					ans.speed += modifs[0];
					break;
				case PC2_CD:
					ans.respawn -= modifs[0];
					break;
				case PC2_COST:
					ans.price -= modifs[0];
					break;
				case PC2_HB:
					ans.hb += modifs[0];
					break;
				case PC2_TBA:
					ans.tba = (int) (ans.tba * (100 - modifs[0]) / 100.0);
			}
		}
	}

	private void improve(CustomUnit ans, int[] type, int[] modifs) {
		if (type[0] == PC_AB)
			ans.abi |= type[1];
		else {
			switch (type[1]) {
				case PC2_SPEED:
					ans.speed += modifs[0];
					break;
				case PC2_CD:
					ans.resp -= modifs[0];
					break;
				case PC2_COST:
					ans.price -= modifs[0];
					break;
				case PC2_HB:
					ans.hb += modifs[0];
					break;
				case PC2_TBA:
					ans.tba = (int) (ans.tba * (100 - modifs[0]) / 100.0);
			}
		}
	}

	public double getStatMultiplication(int mult, int[] talents) {
		for(int i = 0; i < info.size(); i++) {
			if(talents[i] == 0 || info.get(i)[0] >= PC_CORRES.length)
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

	@OnInjected
	public void onInjected() {
		info.replaceAll(data -> {
			if(data.length == 14) {
				return data;
			} else {
				int[] newData = new int[14];
				System.arraycopy(data, 0, newData, 0, data.length);
				return newData;
			}
		});

		max = new int[info.size()];
		for (int i = 0; i < info.size(); i++) {
			max[i] = Math.max(1, info.get(i)[1]);
		}
	}
}
