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
import common.pack.Identifier;
import common.pack.PackData;
import common.pack.UserProfile;
import common.system.files.VFile;
import common.util.Data;
import common.util.anim.AnimU;
import common.util.anim.AnimUD;
import common.util.anim.EAnimU;
import common.util.anim.MaModel;
import common.util.lang.MultiLangCont;
import common.util.stage.MapColc;
import common.util.stage.Stage;
import common.util.stage.StageMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@JsonClass.JCGeneric(Identifier.class)
@JsonClass
public class Enemy extends Character implements AbEnemy {

	@JsonClass.JCIdentifier
	@JsonField
	public final Identifier<AbEnemy> id;
	@JsonField
	public final MaskEnemy de;
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
		if (pack.desc.FORK_VERSION < 4) {
			JsonObject jde = jobj.getAsJsonObject("de");
			inject(pack, jde, enemy);
			AtkDataModel[] atks = enemy.getAllAtkModels();
			Proc proc = enemy.getProc();

			//Updates stuff to match this fork without core version issues
			if (pack.desc.FORK_VERSION < 1) {
				if (UserProfile.isOlderPack(pack, "0.6.4.0")) {
					if (UserProfile.isOlderPack(pack, "0.6.1.0")) {
						if (UserProfile.isOlderPack(pack, "0.5.4.0"))
							enemy.limit = CommonStatic.customEnemyMinPos(anim.loader.getMM());
						//Finish 0.5.4.0 check
						proc.DMGCUT.type.traitIgnore = true;
						proc.DMGCAP.type.traitIgnore = true;
					} //Finish 0.6.1.0 check
					names.put(jobj.get("name").getAsString());
					if (jobj.has("desc"))
						description.put(jobj.get("desc").getAsString().replace("<br>", "\n"));
				} //Finish 6.4.0 check
				for (AtkDataModel ma : atks)
					if (ma.getProc().SUMMON.prob > 0) {
						if (ma.getProc().SUMMON.id != null && !AbEnemy.class.isAssignableFrom(ma.getProc().SUMMON.id.cls))
							ma.getProc().SUMMON.type.fix_buff = true;
						ma.getProc().SUMMON.amount = 1;
					}
				for (AtkDataModel ma : atks)
					if (ma.getProc().SUMMON.prob > 0 && (ma.getProc().SUMMON.id == null || !AbEnemy.class.isAssignableFrom(ma.getProc().SUMMON.id.cls)))
						ma.getProc().SUMMON.form = 1; //There for imports
			} //Finish FORK_VERSION 1 checks
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

	public String getExplanation() {
		String[] desp = MultiLangCont.getDesc(this);
		if (desp != null && desp[1].length() > 0)
			return desp[1].replace("<br>","\n");
		return description.toString();
	}
}