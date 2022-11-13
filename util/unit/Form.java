package common.util.unit;

import com.google.gson.JsonObject;
import common.CommonStatic;
import common.battle.data.*;
import common.io.json.JsonClass;
import common.io.json.JsonClass.*;
import common.io.json.JsonDecoder;
import common.io.json.JsonDecoder.OnInjected;
import common.io.json.JsonField;
import common.pack.Identifier;
import common.pack.PackData;
import common.pack.UserProfile;
import common.system.BasedCopable;
import common.system.VImg;
import common.util.Animable;
import common.util.Data;
import common.util.anim.AnimU;
import common.util.anim.AnimUD;
import common.util.anim.EAnimU;
import common.util.anim.MaModel;
import common.util.lang.MultiLangCont;
import common.util.lang.MultiLangData;

import java.util.ArrayList;

@JCGeneric(AbForm.AbFormJson.class)
@JsonClass(read = RType.FILL)
public class Form extends Animable<AnimU<?>, AnimU.UType> implements BasedCopable<AbForm, AbUnit>, AbForm {

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

	@JsonField(generic = MultiLangData.class)
	public MultiLangData names = new MultiLangData();

	@JsonField(generic = MultiLangData.class)
	public MultiLangData description = new MultiLangData("<br><br><br>");

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
	public int getFid() {
		return fid;
	}

	@Override
	public int getDefaultPrice(int sta) {
		MaskUnit upc = maxu();
		return (int) (upc.getPrice() * (1 + sta * 0.5));
	}

	@Override
	public EAnimU getEAnim(AnimU.UType t) {
		return anim.getEAnim(t);
	}

	public MaskUnit maxu() {
		PCoin pc = du.getPCoin();
		if (pc != null) {
			return pc.full;
		}
		return du;
	}

	@Override
	public VImg getIcon() {
		return anim.getEdi();
	}

	@Override
	public VImg getDeployIcon() {
		return anim.getUni();
	}

	@OnInjected
	public void onInjected(JsonObject jobj) {
		CustomUnit form = (CustomUnit) du;
		form.pack = this;

		if((unit != null || uid != null)) {
			Unit u = unit == null ? (Unit)uid.get() : unit;
			PackData.UserPack pack = (PackData.UserPack) u.getCont();
			if (pack.desc.FORK_VERSION < 2) {
				JsonObject jdu = jobj.getAsJsonObject("du").getAsJsonObject("atks");
				//AtkDataModel[] oldAtks = JsonDecoder.decodePool(jdu, AtkDataModel[].class);
				form.hits.set(0, form.atks);
			}

			AtkDataModel[] atks = form.getAllAtkModels();

			Proc proc = form.getProc();
			if (UserProfile.isOlderPack(pack, "0.6.6.0")) {
				if (UserProfile.isOlderPack(pack, "0.6.5.0")) {
					if (UserProfile.isOlderPack(pack, "0.6.4.0")) {
						if (UserProfile.isOlderPack(pack, "0.6.1.0")) {
							if (UserProfile.isOlderPack(pack, "0.6.0.0")) {
								JsonObject jdu = jobj.getAsJsonObject("du");
								int type = jdu.get("type").getAsInt();
								if (UserProfile.isOlderPack(pack, "0.5.2.0") && form.tba != 0) {
									if (UserProfile.isOlderPack(pack, "0.5.1.0"))
										type = Data.reorderTrait(type);
									//Finish 0.5.1.0 check
									form.tba += form.getPost(0) + 1;
								} //Finish 0.5.2.0 check
								MaModel model = anim.loader.getMM();
								form.limit = CommonStatic.customFormMinPos(model);
								proc.BARRIER.health = jdu.get("shield").getAsInt();
								form.traits = Trait.convertType(type);
								if ((form.abi & (1 << 18)) != 0) //Seal Immunity
									proc.IMUSEAL.mult = 100;
								if ((form.abi & (1 << 7)) != 0) //Moving atk Immunity
									proc.IMUMOVING.mult = 100;
								if ((form.abi & (1 << 12)) != 0) //Poison Immunity
									proc.IMUPOI.mult = 100;
								form.abi = Data.reorderAbi(form.abi, 0);
							} //Finish 0.6.0.0 check
							proc.DMGCUT.reduction = 100;
							for (AtkDataModel atk : atks)
								if (atk.getProc().POISON.prob > 0)
									atk.getProc().POISON.type.ignoreMetal = true;
						} //Finish 0.6.1.0 check
						names.put(jobj.get("name").getAsString());
						if (jobj.has("explanation"))
							description.put(jobj.get("explanation").getAsString());
					} //Finish 0.6.4.0 check
					if ((form.abi & 16) > 0) //2x money
						for (AtkDataModel atk : atks)
							atk.getProc().BOUNTY.mult = 100;
					if ((form.abi & 32) > 0) //base destroyer
						for (AtkDataModel atk : atks)
							atk.getProc().ATKBASE.mult = 300;
					form.abi = Data.reorderAbi(form.abi, 1);
				} //Finish 0.6.5.0 check
				for (AtkDataModel atk : atks)
					if (atk.getProc().TIME.prob > 0)
						atk.getProc().TIME.intensity = atk.getProc().TIME.time;

				for (AtkDataModel atk : atks)
					if (atk.getProc().SUMMON.prob > 0) {
						atk.getProc().SUMMON.max_dis = atk.getProc().SUMMON.dis;
						atk.getProc().SUMMON.min_layer = -1;
						atk.getProc().SUMMON.max_layer = -1;
					}
			} //Finish 0.6.6.0 check

			//Updates stuff to match this fork without core version issues
			if (pack.desc.FORK_VERSION < 1) {
				for (AtkDataModel ma : atks) {
					if (ma.specialTrait && ma.dire == -1)
						ma.traits.addAll(form.traits);
					ma.specialTrait = false;
				} //TODO - Use jsonobject method to remove SpecialTrait

				if ((form.abi & 32) > 0)
					proc.IMUWAVE.block = 100;
				form.abi = Data.reorderAbi(form.abi, 2);
				for (AtkDataModel atk : atks)
					if (atk.getProc().SUMMON.prob > 0) {
						if (!Unit.class.isAssignableFrom(atk.getProc().SUMMON.id.cls))
							atk.getProc().SUMMON.type.fix_buff = true;
						atk.getProc().SUMMON.amount = 1;
					}
			} //Finish FORK_VERSION 1 checks

			for (AtkDataModel atk : atks)
				if (atk.getProc().SUMMON.prob > 0 && atk.getProc().SUMMON.form == 0) {
					atk.getProc().SUMMON.form = 1;
					atk.getProc().SUMMON.mult = 1;
					atk.getProc().SUMMON.type.fix_buff = true;
				}
			}
		if (form.getPCoin() != null)
			form.pcoin.update();
	}

	public ArrayList<Integer> getPrefLvs() {
		ArrayList<Integer> ans;
		final PCoin pc = du instanceof CustomUnit ? du.getPCoin() : unit.forms.length >= 3 ? unit.forms[2].du.getPCoin() : null;

		if (pc != null) {
			ans = new ArrayList<>(pc.max);
			ans.set(0, unit.getPrefLv());
		} else {
			ans = new ArrayList<>();
			ans.add(unit.getPrefLv());
		}

		return ans;
	}

	public ArrayList<Integer> regulateLv(int[] mod, ArrayList<Integer> lv) {
		if (mod != null)
			for (int i = 0; i < Math.min(mod.length, lv.size()); i++)
				lv.set(i, mod[i]);

		int[] maxs = new int[lv.size()];
		maxs[0] = unit.max + unit.maxp;
		PCoin pc = du.getPCoin();
		if (pc != null) {
			for (int i = 0; i < pc.info.size(); i++)
				maxs[i + 1] = Math.max(1, pc.info.get(i)[1]);
		}
		for (int i = 0; i < lv.size(); i++) {
			if (lv.get(i) < 0)
				lv.set(i, 0);
			if (lv.get(i) > maxs[i])
				lv.set(i, maxs[i]);
		}
		if (lv.get(0) == 0)
			lv.set(0, 1);
		return lv;
	}

	@Override
	public String toString() {
		String base = (uid == null ? "NULL" : uid.id) + "-" + fid + " ";
		String desp = MultiLangCont.get(this);
		if (desp != null && desp.length() > 0)
			return base + desp;

		String nam = names.toString();
		if (nam.length() > 0)
			return base + nam;
		return base;
	}

	public String getExplaination() {
		String[] desp = MultiLangCont.getDesc(this);
		if (desp != null && desp[fid + 1].length() > 0)
			return desp[fid + 1];
		return description.toString();
	}
}