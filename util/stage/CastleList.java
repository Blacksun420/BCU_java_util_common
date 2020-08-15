package common.util.stage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import common.pack.FixIndexList.FixIndexMap;
import common.pack.IndexContainer;
import common.pack.PackData.UserPack;
import common.pack.UserProfile;
import common.system.VImg;
import common.system.files.VFile;
import common.util.stage.MapColc.PackMapColc;

public abstract class CastleList extends FixIndexMap<CastleImg> implements IndexContainer {

	public static class DefCasList extends CastleList {

		public final String str;
		public final String id;

		public DefCasList(String hash, String name) {
			id = hash;
			str = name;
			map().put(id, this);
			defset().add(this);
			for (VFile<?> vf : VFile.get("./org/img/" + name).list())
				add(new CastleImg(getNextID(CastleImg.class), new VImg(vf)));

		}

		@Override
		public String getID() {
			return id;
		}

		@Override
		public String toString() {
			return str + " (" + size() + ")";
		}
	}

	public static class PackCasList extends CastleList {

		public final UserPack pack;

		public PackCasList(UserPack p) {
			pack = p;
			map().put(pack.desc.id, this);
		}

		@Override
		public String getID() {
			return pack.getID();
		}

		@Override
		public String toString() {
			return pack.desc.name + " (" + size() + ")";
		}
	}

	private static final String REG_CASTLE = "castle";

	private static final String REG_DEF_CASTLE = "def_castle";

	public static Set<CastleList> defset() {
		return UserProfile.getPool(REG_DEF_CASTLE, CastleList.class);
	}

	public static Collection<CastleList> from(Stage sta) {
		MapColc mc = sta.map.mc;
		if (!(mc instanceof PackMapColc))
			return defset();
		List<CastleList> list = new ArrayList<>();
		list.addAll(defset());
		UserPack pack = ((PackMapColc) mc).pack;
		list.add(pack.castles);
		for (String rel : pack.desc.dependency)
			list.add(UserProfile.getUserPack(rel).castles);
		return list;
	}

	@ContGetter
	public static CastleList getList(String str) {
		return map().get(str);
	}

	public static Map<String, CastleList> map() {
		return UserProfile.getRegister(REG_CASTLE, CastleList.class);
	}

	private CastleList() {
		super(CastleImg.class);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public <R> R getList(Class cls, Reductor<R, FixIndexMap> func, R def) {
		return func.reduce(def, this);
	}

}
