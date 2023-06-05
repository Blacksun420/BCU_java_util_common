package common.util.unit;

import com.google.gson.JsonObject;
import common.CommonStatic;
import common.battle.data.*;
import common.io.json.JsonClass;
import common.io.json.JsonClass.JCConstructor;
import common.io.json.JsonClass.JCGeneric;
import common.io.json.JsonClass.RType;
import common.io.json.JsonDecoder.OnInjected;
import common.io.json.JsonField;
import common.pack.Context;
import common.pack.Identifier;
import common.pack.PackData;
import common.pack.UserProfile;
import common.system.BasedCopable;
import common.system.VImg;
import common.util.Data;
import common.util.anim.AnimU;
import common.util.anim.AnimUD;
import common.util.anim.MaModel;
import common.util.lang.MultiLangCont;

import javax.annotation.Nullable;
import java.util.Arrays;

@JCGeneric(AbForm.AbFormJson.class)
@JsonClass(read = RType.FILL)
public class Form extends Character implements BasedCopable<AbForm, AbUnit>, AbForm, Comparable<AbForm> {

	public static String lvString(int[] lvs) {
		StringBuilder str = new StringBuilder("Lv." + lvs[0] + ", {");
		for (int i = 1; i < 5; i++)
			str.append(lvs[i]).append(",");
		str.append(lvs[5]).append("}");
		return str.toString();
	}

	@JsonField
	public final MaskUnit du;
	public final Unit unit;
	public final Identifier<AbUnit> uid;
	@JsonField
	public int fid;
	public Orb orbs = null;

	@JCConstructor
	public Form(Unit u) {
		du = null;
		unit = u;
		uid = unit.id;
		orbs = new Orb(-1);
	}

	public Form(Unit u, int f, String str, AnimU<?> ac, CustomUnit cu) {
		unit = u;
		uid = u.id;
		fid = f;
		names.put(str);
		anim = ac;
		du = cu;
		cu.pack = this;
		orbs = new Orb(-1);
	}

	//Used for BC units
	protected Form(Unit u, int f, String str, String data) {
		unit = u;
		uid = u.id;
		fid = f;
		String nam = trio(uid.id) + "_" + SUFX[fid];
		anim = new AnimUD(str, nam, "edi" + nam + ".png", "uni" + nam + "00.png");
		anim.getUni().setCut(CommonStatic.getBCAssets().unicut);
		String[] strs = data.split("//")[0].trim().split(",");
		du = new DataUnit(this, strs);
		MaModel model = anim.loader.getMM();
		((DataUnit) du).limit = CommonStatic.dataFormMinPos(model);
	}
	//Used for BC eggs
	protected Form(Unit u, int f, int m, String str, String data) {
		unit = u;
		uid = u.id;
		fid = f;
		String nam = trio(m) + "_m";
		anim = new AnimUD(str, nam, "edi" + nam + duo(fid) + ".png", "uni" + nam + duo(fid) + ".png");
		anim.getUni().setCut(CommonStatic.getBCAssets().unicut);
		String[] strs = data.split("//")[0].trim().split(",");
		du = new DataUnit(this, strs);
		MaModel model = anim.loader.getMM();
		((DataUnit) du).limit = CommonStatic.dataFormMinPos(model);
	}

	@Override
	public Form copy(AbUnit b) {
		CustomUnit cu = new CustomUnit(anim);
		cu.importData(du);
		return new Form((Unit) b, fid, names.toString(), anim, cu);
	}

	@Override
	public Identifier<AbUnit> getID() {
		return uid;
	}

	@Override
	public Unit unit() {
		return unit;
	}

	@Override
	public int getFid() {
		return fid;
	}

	public MaskUnit maxu() {
		PCoin pc = du.getPCoin();
		if (pc != null)
			return pc.full;
		return du;
	}

	@Override
	public VImg getDeployIcon() {
		return anim.getUni();
	}

	public MaskUnit getMask() {
		return du;
	}

	@OnInjected
	public void onInjected(JsonObject jobj) {
		CustomUnit form = (CustomUnit) du;
		form.pack = this;

		if ((unit != null || uid != null)) {
			Unit u = unit == null ? (Unit) uid.get() : unit;
			PackData.UserPack pack = (PackData.UserPack) u.getCont();
			if (pack.desc.FORK_VERSION < 7) {
				if (pack.desc.FORK_VERSION < 6) {
					inject(pack, jobj.getAsJsonObject("du"), form);
					if (pack.desc.FORK_VERSION < 1) {
						AtkDataModel[] atks = form.getAllAtkModels();
						if (UserProfile.isOlderPack(pack, "0.6.4.0")) {
							if (UserProfile.isOlderPack(pack, "0.6.0.0"))
								form.limit = CommonStatic.customFormMinPos(anim.loader.getMM());
							//Finish 0.6.0.0 check
							names.put(jobj.get("name").getAsString());
							if (jobj.has("explanation"))
								description.put(jobj.get("explanation").getAsString().replace("<br>", "\n"));
						} //Finish 0.6.4.0 check
						for (AtkDataModel atk : atks)
							if (atk.getProc().SUMMON.prob > 0) {
								if (atk.getProc().SUMMON.id != null && !Unit.class.isAssignableFrom(atk.getProc().SUMMON.id.cls))
									atk.getProc().SUMMON.type.fix_buff = true;
								atk.getProc().SUMMON.amount = 1;
							}
						for (AtkDataModel atk : atks)
							if (atk.getProc().SUMMON.prob > 0 && atk.getProc().SUMMON.form == 0) {
								atk.getProc().SUMMON.form = 1;
								atk.getProc().SUMMON.mult = 1;
								atk.getProc().SUMMON.type.fix_buff = true;
							}
					} //Finish FORK_VERSION 1 checks
				} //Finish FORK_VERSION 6 checks
				if (form.getPCoin() != null) {
					form.pcoin.info.replaceAll(data -> {
						int[] corres = Data.get_CORRES(data[0]);
						int[] trueArr;
						switch (corres[0]) {
							case Data.PC_P:
								trueArr = Arrays.copyOf(data, 3 + (du.getProc().getArr(corres[1]).getDeclaredFields().length - (corres.length >= 3 ? corres[2] : 0)) * 2); //The Math.min is for testing
								break;
							case Data.PC_BASE:
								trueArr = Arrays.copyOf(data, 5);
								break;
							default:
								trueArr = Arrays.copyOf(data, 3);
						}
						if (data.length == 14)
							trueArr[trueArr.length - 1] = Math.max(0, data[13]); //super talent lv
						return trueArr;
					});
				}
			} //Finish FORK_VERSION 7 checks
		}
		if (form.getPCoin() != null)
			form.pcoin.update();
	}

	/**
	 * Validate level values in {@code target} {@link common.util.unit.Level}
	 * @param src {@code Level} that will be put into {@code target} {@code Level}. Can be null
	 * @param target {@code Level} that will be validated
	 * @return Validated {@code target} {@code Level} will be returned
	 */
	@Override
	public Level regulateLv(@Nullable Level src, Level target) {
		if(src != null) {
			target.setLevel(Math.max(1, Math.min(src.getLv(), unit.max)));
			target.setPlusLevel(Math.max(0, Math.min(src.getPlusLv(), unit.maxp)));

			PCoin pc = du.getPCoin();

			if (pc != null) {
				int[] maxTalents = new int[pc.info.size()];

				for (int i = 0; i < pc.info.size(); i++)
					maxTalents[i] = Math.max(1, pc.info.get(i)[1]);

				int[] t = new int[maxTalents.length];

				for (int i = 0; i < Math.min(maxTalents.length, src.getTalents().length); i++) {
					t[i] = Math.min(maxTalents[i], Math.max(0, src.getTalents()[i]));
				}

				if (src.getTalents().length < target.getTalents().length) {
					for (int i = src.getTalents().length; i < Math.min(maxTalents.length, target.getTalents().length); i++) {
						t[i] = Math.min(maxTalents[i], Math.min(0, target.getTalents()[i]));
					}
				}

				target.setTalents(t);
			}
		} else {
			target.setLevel(Math.max(1, Math.min(unit.max, target.getLv())));
			target.setPlusLevel(Math.max(0, Math.min(unit.maxp, target.getPlusLv())));

			PCoin pc = du.getPCoin();

			if (pc != null) {
				int[] maxTalents = new int[pc.info.size()];
				int[] t = new int[pc.info.size()];

				for (int i = 0; i < pc.info.size(); i++)
					maxTalents[i] = Math.max(1, pc.info.get(i)[1]);

				for (int i = 0; i < Math.min(maxTalents.length, target.getTalents().length); i++) {
					t[i] = Math.min(maxTalents[i], Math.max(0, target.getTalents()[i]));
				}

				target.setTalents(t);
			}
		}
		return target;
	}

	@Override
	public String toString() {
		String base = (uid == null ? "NULL" : uid.id) + "-" + fid + " ";
		if (CommonStatic.getFaves().units.contains(this))
			base = "❤" + base;
		String desp = MultiLangCont.get(this);
		if (desp != null && desp.length() > 0)
			return base + desp;

		String nam = names.toString();
		if (nam.length() > 0)
			return base + nam;
		return base;
	}

	public String getExplanation() {
		String[] desp = MultiLangCont.getDesc(this);
		if (desp != null && desp[fid + 1].length() > 0)
			return desp[fid + 1].replace("<br>","\n");
		return description.toString();
	}

	public int compareTo(AbForm u) {
		int i = getID().compareTo(u.getID());
		if (i == 0)
			return Integer.compare(fid, u.getFid());
		return i;
	}

	public boolean hasEvolveCost() {
		return unit.info.hasEvolveCost() && fid == 2;
	}
}