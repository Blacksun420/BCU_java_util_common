package common.util.stage;

import common.CommonStatic;
import common.io.json.JsonClass;
import common.io.json.JsonField;
import common.pack.Identifier;
import common.pack.IndexContainer.IndexCont;
import common.pack.IndexContainer.Indexable;
import common.pack.PackData;
import common.system.files.FileData;
import common.util.Data;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@JsonClass
@IndexCont(PackData.class)
@JsonClass.JCGeneric(Identifier.class)
public class Music implements Indexable<PackData, Music>, Comparable<Music> {

	@JsonField
	@JsonClass.JCIdentifier
	public final Identifier<Music> id;
	@JsonField
	public long loop;
	@JsonField
	public String name = "";

	public FileData data;

	@JsonClass.JCConstructor
	@Deprecated
	public Music() {
		id = null;
	}

	public Music(Identifier<Music> id, FileData fd) {
		this.id = id;
		data = fd;
	}

	public Music(Identifier<Music> id, FileData fd, Music m) {
		this(id, fd);
		if (m != null) {
			loop = m.loop;
			name = m.name;
		}
	}

	@Override
	public Identifier<Music> getID() {
		return id;
	}

	@Override
	public String toString() {
		if (id != null) {
			if (!name.isEmpty())
				return Data.trio(id.id) + ".ogg - " + name + " (" + id.pack + ")";
			return Data.trio(id.id) + ".ogg - " + id.pack;
		} else
			return name;
	}

	@Override
	public int compareTo(@NotNull Music o) {
		if (id == null) {
			if (o.id == null)
				return 0;
			return -1;
		} else if (o.id == null)
			return 1;
		return id.compareTo(o.id);
	}

	public static boolean valid(String str) {
		if (str.length() != 7 || !str.contains(Data.trio(CommonStatic.parseIntN(str))))
			return false;
		return isMusic(str);
	}

	public static boolean isMusic(String str) {
		return str.endsWith(".ogg");
	}

	public ArrayList<Stage> getStages() {
		ArrayList<Stage> ans = new ArrayList<>();
		for (Stage st : MapColc.getAllStage()) {
			if (st != null && (st.mus0 != null && st.mus0.equals(id) || st.mus1 != null && st.mus1.equals(id)))
				ans.add(st);
		}
		return ans;
	}
}