package common.util.unit;

import com.google.gson.JsonObject;
import common.CommonStatic;
import common.battle.StageBasis;
import common.battle.data.AtkDataModel;
import common.battle.data.CustomEnemy;
import common.battle.data.DataEnemy;
import common.battle.data.MaskEnemy;
import common.battle.entity.EEnemy;
import common.io.json.JsonClass;
import common.io.json.JsonDecoder.OnInjected;
import common.io.json.JsonField;
import common.io.json.localDecoder;
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
import common.util.stage.MapColc;
import common.util.stage.Stage;
import common.util.stage.StageMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
		EAnimU anim = getEAnim(AnimU.TYPEDEF[AnimU.ENTRY]);
		if (anim.unusable())
			anim = getEAnim(AnimU.TYPEDEF[AnimU.WALK]);

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

		PackData.UserPack pack = (PackData.UserPack) getCont();

		if (pack.desc.FORK_VERSION < 3) {
			JsonObject jdu = jobj.getAsJsonObject("de");
			if (pack.desc.FORK_VERSION < 2) {
				AtkDataModel[] oldAtks = new localDecoder(jdu.getAsJsonObject("atks"), AtkDataModel[].class, enemy).setGen(true).setPool(true).decode();
				enemy.hits.set(0, oldAtks);
			}

			AtkDataModel oldSpAtk = new localDecoder(jdu.get("rev"), AtkDataModel.class, enemy).setGen(true).decode();
			if (oldSpAtk != null)
				enemy.revs = new AtkDataModel[]{oldSpAtk};
			oldSpAtk = new localDecoder(jdu.get("res"), AtkDataModel.class, enemy).setGen(true).decode();
			if (oldSpAtk != null)
				enemy.ress = new AtkDataModel[]{oldSpAtk};
			oldSpAtk = new localDecoder(jdu.get("bur"), AtkDataModel.class, enemy).setGen(true).decode();
			if (oldSpAtk != null)
				enemy.burs = new AtkDataModel[]{oldSpAtk};
			oldSpAtk = new localDecoder(jdu.get("resu"), AtkDataModel.class, enemy).setGen(true).decode();
			if (oldSpAtk != null)
				enemy.resus = new AtkDataModel[]{oldSpAtk};
			oldSpAtk = new localDecoder(jdu.get("revi"), AtkDataModel.class, enemy).setGen(true).decode();
			if (oldSpAtk != null)
				enemy.revis = new AtkDataModel[]{oldSpAtk};
			oldSpAtk = new localDecoder(jdu.get("entr"), AtkDataModel.class, enemy).setGen(true).decode();
			if (oldSpAtk != null)
				enemy.entrs = new AtkDataModel[]{oldSpAtk};
		}

		Proc proc = enemy.getProc();
		AtkDataModel[] atks = enemy.getAllAtkModels();

		if (UserProfile.isOlderPack(pack, "0.6.6.0")) {
			if (UserProfile.isOlderPack(pack, "0.6.5.0")) {
				if (UserProfile.isOlderPack(pack, "0.6.4.0")) {
					if (UserProfile.isOlderPack(pack, "0.6.1.0")) {
						if (UserProfile.isOlderPack(pack, "0.6.0.0")) {
							JsonObject jde = jobj.getAsJsonObject("de");
							proc.BARRIER.health = jde.get("shield").getAsInt();
							int type = jde.get("type").getAsInt();
							if (UserProfile.isOlderPack(pack, "0.5.4.0")) {
								if (UserProfile.isOlderPack(pack, "0.5.2.0") && enemy.tba != 0) {
									if (UserProfile.isOlderPack(pack, "0.5.1.0"))
										type = Data.reorderTrait(type);
									//Finish 5.1.0 check
									enemy.tba += enemy.getPost(false, 0) + 1;
								} //Finish 5.2.0 check
								MaModel model = anim.loader.getMM();
								enemy.limit = CommonStatic.customEnemyMinPos(model);
							} //Finish 5.4.0 check
							enemy.traits = Trait.convertType(type);
							if ((enemy.abi & (1 << 18)) != 0) //Seal Immunity
								proc.IMUSEAL.mult = 100;
							if ((enemy.abi & (1 << 7)) != 0) //Moving atk Immunity
								proc.IMUMOVING.mult = 100;
							if ((enemy.abi & (1 << 12)) != 0) //Poison Immunity
								proc.IMUPOI.mult = 100;
							enemy.abi = Data.reorderAbi(enemy.abi, 0);
						} //Finish 6.0.0 check
						proc.DMGCUT.reduction = 100;
						proc.DMGCUT.type.traitIgnore = true;
						proc.DMGCAP.type.traitIgnore = true;
						for (AtkDataModel atk : atks)
							if (atk.getProc().POISON.prob > 0)
								atk.getProc().POISON.type.ignoreMetal = true;
					} //Finish 6.1.0 check
					names.put(jobj.get("name").getAsString());
					if (jobj.has("desc"))
						description.put(jobj.get("desc").getAsString());
				} //Finish 6.4.0 check
				if ((enemy.abi & 32) > 0) //base destroyer
					for (AtkDataModel atk : atks)
						atk.getProc().ATKBASE.mult = 300;
				enemy.abi = Data.reorderAbi(enemy.abi, 1);
			} //Finish 6.5.0 check
			for (AtkDataModel atk : atks)
				if (atk.getProc().TIME.prob > 0)
					atk.getProc().TIME.intensity = atk.getProc().TIME.time;

			for (AtkDataModel atk : atks)
				if (atk.getProc().SUMMON.prob > 0) {
					atk.getProc().SUMMON.max_dis = atk.getProc().SUMMON.dis;
					atk.getProc().SUMMON.min_layer = -1;
					atk.getProc().SUMMON.max_layer = -1;
				}
		} //Finish 6.6.0 check

		//Updates stuff to match this fork without core version issues
		if (pack.desc.FORK_VERSION < 1) {
			if ((enemy.abi & 32) > 0)
				proc.IMUWAVE.block = 100;
			enemy.abi = Data.reorderAbi(enemy.abi, 2);
			for (AtkDataModel ma : atks)
				if (ma.getProc().SUMMON.prob > 0) {
					if (ma.getProc().SUMMON.id != null && !AbEnemy.class.isAssignableFrom(ma.getProc().SUMMON.id.cls))
						ma.getProc().SUMMON.type.fix_buff = true;
					ma.getProc().SUMMON.amount = 1;
				}
		} //Finish FORK_VERSION 1 checks

		for (AtkDataModel ma : atks)
			if (ma.getProc().SUMMON.prob > 0 && (ma.getProc().SUMMON.id == null || !AbEnemy.class.isAssignableFrom(ma.getProc().SUMMON.id.cls)))
				ma.getProc().SUMMON.form = 1; //There for imports
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