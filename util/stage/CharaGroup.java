package common.util.stage;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import common.io.json.JsonClass;
import common.io.json.JsonClass.JCGeneric;
import common.io.json.JsonClass.JCIdentifier;
import common.io.json.JsonClass.NoTag;
import common.io.json.JsonDecoder;
import common.io.json.JsonField;
import common.pack.Identifier;
import common.pack.IndexContainer.IndexCont;
import common.pack.IndexContainer.Indexable;
import common.pack.PackData;
import common.pack.PackData.UserPack;
import common.pack.UserProfile;
import common.util.Data;
import common.util.unit.AbForm;
import common.util.unit.AbUnit;
import common.util.unit.Form;
import common.util.unit.Unit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

@IndexCont(PackData.class)
@JsonClass(noTag = NoTag.LOAD)
@JCGeneric(Identifier.class)
public class CharaGroup extends Data implements Indexable<PackData, CharaGroup>, Comparable<CharaGroup> {

	public String name = "";

	@JCIdentifier
	public Identifier<CharaGroup> id;

	public int type = 0;

	@JsonField(generic = Form.class, alias = AbForm.AbFormJson.class)
		public final ArrayList<Form> fset = new ArrayList<>();

	@JsonClass.JCConstructor
	public CharaGroup() {

	}

	public CharaGroup(CharaGroup cg) {
		type = cg.type;
		fset.addAll(cg.fset);
	}

	public CharaGroup(Identifier<CharaGroup> id) {
		this.id = id;
	}

	public CharaGroup(int ID, int t, Identifier<AbUnit>[] units) {
		type = t;
		for (Identifier<AbUnit> uid : units) {
			AbUnit u = Identifier.get(uid);
			if (u != null)
				fset.addAll(Arrays.asList(u.getForms()));
		}
		id = Identifier.parseInt(ID, CharaGroup.class);
	}

	public boolean allow(Form f) {
		return (type != 0 || fset.contains(f)) && (type != 2 || !fset.contains(f));
	}

	public CharaGroup combine(CharaGroup cg) {
		CharaGroup ans = new CharaGroup(this);
		if (type == 0 && cg.type == 0)
			ans.fset.retainAll(cg.fset);
		else if (type == 0 && cg.type == 2)
			ans.fset.removeAll(cg.fset);
		else if (type == 2 && cg.type == 0) {
			ans.type = 0;
			ans.fset.addAll(cg.fset);
			ans.fset.removeAll(fset);
		} else if (type == 2 && cg.type == 2)
			ans.fset.addAll(cg.fset);
		return ans;
	}

	@Override
	public int compareTo(CharaGroup cg) {
		return id.compareTo(cg.id);
	}

	@Override
	public Identifier<CharaGroup> getID() {
		return id;
	}

	@Override
	public String toString() {
		return id + " - " + name;
	}

	public boolean used() {
		UserPack mc = (UserPack) getCont();
		for (LvRestrict lr : mc.lvrs.getList())
			if (lr.res.containsKey(this))
				return true;
		for (StageMap sm : mc.mc.maps)
			for (Stage st : sm.list)
				if (st.lim != null && st.lim.group == this)
					return true;
		return false;
	}

	@JsonDecoder.OnInjected
	public void onInjected(JsonObject jobj) {
		UserPack pack = (UserPack) getCont();
		if (UserProfile.isOlderPack(pack, "0.6.8.2")) {
			JsonArray jarr = jobj.get("set").getAsJsonArray();
			for (int i = 0; i < jarr.size(); i++) {
				String pacc = jarr.get(i).getAsJsonObject().get("pack").getAsString();
				int id = jarr.get(i).getAsJsonObject().get("id").getAsInt();
				Unit u = (Unit) new Identifier<>(pacc, Unit.class, id).get();
				if (u != null)
					Collections.addAll(fset, u.forms);
			}
		}
	}
}
