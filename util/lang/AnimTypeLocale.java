package common.util.lang;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import common.CommonStatic;
import common.util.pack.Background;
import common.util.pack.EffAnim;
import common.util.pack.NyCastle;
import common.util.pack.WaveAnim;
import common.io.assets.Admin.StaticPermitted;
import common.util.anim.AnimI;
import common.util.anim.AnimU.UType;

public class AnimTypeLocale {

	@StaticPermitted
	public static final Set<AnimI.AnimType<?, ?>> TYPES = new HashSet<>();

	static {
		Collections.addAll(TYPES, Background.BGWvType.values());
		Collections.addAll(TYPES, NyCastle.NyType.values());
		Collections.addAll(TYPES, UType.values());
		Collections.addAll(TYPES, WaveAnim.WaveType.values());
		Collections.addAll(TYPES, EffAnim.ArmorEff.values());
		Collections.addAll(TYPES, EffAnim.BarrierEff.values());
		Collections.addAll(TYPES, EffAnim.DefEff.values());
		Collections.addAll(TYPES, EffAnim.KBEff.values());
		Collections.addAll(TYPES, EffAnim.SniperEff.values());
		Collections.addAll(TYPES, EffAnim.SpeedEff.values());
		Collections.addAll(TYPES, EffAnim.VolcEff.values());
		Collections.addAll(TYPES, EffAnim.WarpEff.values());
		Collections.addAll(TYPES, EffAnim.WeakUpEff.values());
		Collections.addAll(TYPES, EffAnim.ZombieEff.values());
		Collections.addAll(TYPES, EffAnim.ShieldEff.values());
		Collections.addAll(TYPES, EffAnim.DmgCap.values());
		Collections.addAll(TYPES, EffAnim.LethargyEff.values());
	}

	public static void read() {
		String loc = CommonStatic.Lang.LOC_CODE[CommonStatic.getConfig().lang];
		InputStream f;

		switch (CommonStatic.getConfig().lang) {
			case 2:
				f = CommonStatic.ctx.getLangFile("animation_type_kr.json");
				break;
			case 3:
				f = CommonStatic.ctx.getLangFile("animation_type_jp.json");
				break;
			default:
				f = CommonStatic.ctx.getLangFile("animation_type.json");
				break;
		}

		JsonElement je = JsonParser.parseReader(new InputStreamReader(f, StandardCharsets.UTF_8));
		for (AnimI.AnimType<?, ?> type : TYPES) {
			JsonObject obj = je.getAsJsonObject().get(type.getClass().getSimpleName()).getAsJsonObject();
			String val = obj.get(type.toString()).getAsString();
			MultiLangCont.getStatic().ANIMNAME.put(loc, type, val);
		}
	}

}
