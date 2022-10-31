package common.util.unit;

import com.google.gson.JsonObject;
import common.CommonStatic;
import common.battle.StageBasis;
import common.battle.data.*;
import common.util.stage.MapColc;
import common.util.stage.Stage;
import common.util.stage.StageMap;
import common.battle.entity.EEnemy;
import common.io.json.JsonClass;
import common.io.json.JsonDecoder.OnInjected;
import common.io.json.JsonField;
import common.pack.Identifier;
import common.pack.PackData;
import common.pack.UserProfile;
import common.system.VImg;
import common.system.files.VFile;
import common.util.Animable;
import common.util.Data;
import common.util.anim.AnimU;
import common.util.anim.AnimU.UType;
import common.util.anim.AnimUD;
import common.util.anim.EAnimU;
import common.util.anim.MaModel;
import common.util.lang.MultiLangCont;
import common.util.lang.MultiLangData;

import java.util.*;

@JsonClass.JCGeneric(Identifier.class)
@JsonClass
public class Enemy extends Animable<AnimU<?>, UType> implements AbEnemy {

	@JsonClass.JCIdentifier
	@JsonField
	public final Identifier<AbEnemy> id;
	@JsonField
	public final MaskEnemy de;

	@JsonField(generic = MultiLangData.class)
	public MultiLangData names = new MultiLangData();

	@JsonField(generic = MultiLangData.class)
	public MultiLangData description = new MultiLangData("<br><br><br>");

	public boolean inDic = false;

	@JsonClass.JCConstructor
	public Enemy() {
		id = null;
		de = null;
	}

	public Enemy(Identifier<AbEnemy> hash, AnimU<?> ac, CustomEnemy ce) {
		id = hash;
		de = ce;
		ce.pack = this;
		anim = ac;
	}

	public Enemy(VFile f) {
		id = new Identifier<>(Identifier.DEF, Enemy.class, CommonStatic.parseIntN(f.getName()));
		String str = "./org/enemy/" + Data.trio(id.id) + "/";
		de = new DataEnemy(this);
		anim = new AnimUD(str, Data.trio(id.id) + "_e", "edi_" + Data.trio(id.id) + ".png", null);
		anim.getEdi().check();
		MaModel model = anim.loader.getMM();
		((DataEnemy) de).limit = CommonStatic.dataEnemyMinPos(model);
	}

	public List<Stage> findApp() {
		List<Stage> ans = new ArrayList<>();
		for (Stage st : MapColc.getAllStage()) {
			if (st != null && st.contains(this))
				ans.add(st);
		}
		return ans;
	}

	public List<Stage> findApp(MapColc mc) {
		List<Stage> ans = new ArrayList<>();
		for (StageMap sm : mc.maps)
			for (Stage st : sm.list)
				if (st.contains(this))
					ans.add(st);
		return ans;
	}

	public List<MapColc> findMap() {
		List<MapColc> ans = new ArrayList<>();
		for (MapColc mc : MapColc.values()) {
			if (mc instanceof MapColc.PackMapColc)
				continue;
			boolean col = false;
			for (StageMap sm : mc.maps) {
				for (Stage st : sm.list)
					if (col = st.contains(this)) {
						ans.add(mc);
						break;
					}
				if (col)
					break;
			}
		}
		return ans;
	}

	@Override
	public EAnimU getEAnim(UType t) {
		if (anim == null)
			return null;
		return anim.getEAnim(t);
	}

	@Override
	public EEnemy getEntity(StageBasis b, Object obj, double hpMagnif, double atkMagnif, int d0, int d1, int m) {
		hpMagnif *= de.multi(b.b);
		atkMagnif *= de.multi(b.b);
		EAnimU anim = getEntryAnim();
		return new EEnemy(b, de, anim, hpMagnif, atkMagnif, d0, d1, m);
	}

	public EAnimU getEntryAnim() {
		EAnimU anim = getEAnim(AnimU.UType.ENTER);
		if (anim.unusable())
			anim = getEAnim(AnimU.UType.WALK);

		anim.setTime(0);
		return anim;
	}

	@Override
	public VImg getIcon() {
		if(anim == null)
			return null;

		return anim.getEdi();
	}

	@Override
	public Identifier<AbEnemy> getID() {
		return id;
	}

	@Override
	public Set<Enemy> getPossible() {
		Set<Enemy> te = new TreeSet<>();
		te.add(this);
		return te;
	}

	@OnInjected
	public void onInjected(JsonObject jobj) {
		CustomEnemy enemy = (CustomEnemy) de;
		enemy.pack = this;

		if(getCont() instanceof PackData.UserPack) {
			PackData.UserPack pack = (PackData.UserPack) getCont();
			if (UserProfile.isOlderPack(pack, "0.6.9.2")) {
				if (UserProfile.isOlderPack(pack, "0.6.9.1")) {
					JsonObject jde = jobj.getAsJsonObject("de");
					if (UserProfile.isOlderPack(pack, "0.6.6.0")) {
						if (UserProfile.isOlderPack(pack, "0.6.5.0")) {
							if (UserProfile.isOlderPack(pack, "0.6.4.0")) {
								if (UserProfile.isOlderPack(pack, "0.6.1.0")) {
									if (UserProfile.isOlderPack(pack, "0.6.0.0")) {
										enemy.getProc().BARRIER.health = jde.get("shield").getAsInt();
										int type = jde.get("type").getAsInt();
										if (UserProfile.isOlderPack(pack, "0.5.4.0")) {
											if (UserProfile.isOlderPack(pack, "0.5.2.0") && enemy.tba != 0) {
												enemy.tba += enemy.getPost() + 1;
											}
											if (UserProfile.isOlderPack(pack, "0.5.1.0")) {
												type = Data.reorderTrait(type);
											}
											MaModel model = anim.loader.getMM();
											enemy.limit = CommonStatic.customEnemyMinPos(model);
										}
										enemy.traits = Trait.convertType(type);

										Proc proc = enemy.getProc();
										if ((enemy.abi & (1 << 18)) != 0) //Seal Immunity
											proc.IMUSEAL.mult = 100;
										if ((enemy.abi & (1 << 7)) != 0) //Moving atk Immunity
											proc.IMUMOVING.mult = 100;
										if ((enemy.abi & (1 << 12)) != 0) //Poison Immunity
											proc.IMUPOI.mult = 100;
										enemy.abi = Data.reorderAbi(enemy.abi, 0);
									} //Finish 6.0.0 check
									enemy.getProc().DMGCUT.reduction = 100;
									enemy.getProc().DMGCUT.type.traitIgnore = true;
									enemy.getProc().DMGCAP.type.traitIgnore = true;
									enemy.getProc().POISON.type.ignoreMetal = true;
								} //Finish 6.1.0 check
								names.put(jobj.get("name").getAsString());
								if (jobj.has("desc"))
									description.put(jobj.get("desc").getAsString());
							} //Finish 6.4.0 check
							Proc proc = enemy.getProc();

							if ((enemy.abi & 32) > 0) //base destroyer
								proc.ATKBASE.mult = 300;
							enemy.abi = Data.reorderAbi(enemy.abi, 1);
						} //Finish 6.5.0 check
						if (enemy.getProc().TIME.prob > 0)
							enemy.getProc().TIME.intensity = enemy.getProc().TIME.time;

						if (enemy.getProc().SUMMON.prob > 0) {
							enemy.getProc().SUMMON.max_dis = enemy.getProc().SUMMON.dis;
							enemy.getProc().SUMMON.min_layer = -1;
							enemy.getProc().SUMMON.max_layer = -1;
						}
					} //Finish 6.6.0 check
					if (enemy.rep.specialTrait && enemy.rep.dire == -1)
						enemy.rep.traits.addAll(enemy.traits);
					enemy.rep.specialTrait = false;
					for (MaskAtk ma : enemy.getAtks()) {
						AtkDataModel adm = (AtkDataModel) ma;
						if ((adm.specialTrait && adm.dire == 1) || (!adm.specialTrait && adm.dire == -1))
							adm.traits.addAll(enemy.traits);
						adm.specialTrait = false;
					}
					for (AtkDataModel adm : enemy.getSpAtks()) {
						if (adm == null)
							continue;

						if ((adm.specialTrait && adm.dire == 1) || (!adm.specialTrait && adm.dire == -1))
							adm.traits.addAll(enemy.traits);
						adm.specialTrait = false;
					}
				} //Finish 6.8.1 check
				if ((enemy.abi & 32) > 0)
					enemy.getProc().IMUWAVE.block = 100;
				enemy.abi = Data.reorderAbi(enemy.abi, 2);
			} //Finish 6.8.2 check

			if (enemy.getProc().SUMMON.prob > 0 && (enemy.getProc().SUMMON.id == null || !AbEnemy.class.isAssignableFrom(enemy.getProc().SUMMON.id.cls)))
				enemy.getProc().SUMMON.form = 1; //There for imports
		}
	}

	@Override
	public String toString() {
		String desp = MultiLangCont.get(this);
		if (desp != null && desp.length() > 0)
			return Data.trio(id.id) + " - " + desp;

		String nam = names.toString();
		if (nam.length() == 0)
			return Data.trio(id.id);
		return Data.trio(id.id) + " - " + nam;
	}

	public String getExplaination() {
		String[] desp = MultiLangCont.getDesc(this);
		if (desp != null && desp[1].length() > 0)
			return desp[1];
		return description.toString();
	}
}