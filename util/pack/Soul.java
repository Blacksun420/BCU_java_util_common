package common.util.pack;

import common.io.json.JsonDecoder;
import common.io.json.JsonField;
import common.util.anim.AnimU;
import common.util.anim.EAnimI;
import common.util.stage.Music;
import common.io.json.JsonClass;
import common.pack.Identifier;
import common.pack.IndexContainer.IndexCont;
import common.pack.IndexContainer.Indexable;
import common.pack.PackData;
import common.util.Animable;

@JsonClass(noTag = JsonClass.NoTag.LOAD)
@IndexCont(PackData.class)
@JsonClass.JCGeneric(Identifier.class)
public class Soul extends Animable<AnimU<?>, AnimU.UType> implements Indexable<PackData, Soul> {

	@JsonClass.JCIdentifier
	private final Identifier<Soul> id;

	public Identifier<Music> audio;

	public String name;

	@JsonField(defval = "true")
	public boolean fixedLayer = true;

	public int layer;

	@JsonClass.JCConstructor
	public Soul() {
		id = null;
	}

	public Soul(Identifier<Soul> id, AnimU<?> animS) {
		anim = animS;
		this.id = id;

		if (fromBC())
			name = "soul " + id.id;
		else
			name = "custom soul " + id.id;
	}

	@Override
	public Identifier<Soul> getID() {
		return id;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public EAnimI getEAnim(AnimU.UType uType) {
		return anim.getEAnim(uType);
	}

	@JsonDecoder.OnInjected
	public void onInjected() {
		PackData.UserPack pack = (PackData.UserPack)getCont();
		if (pack.desc.FORK_VERSION < 15)
			fixedLayer = false;
	}
}
