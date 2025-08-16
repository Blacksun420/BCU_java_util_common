package common.util.unit;

import com.google.gson.JsonObject;
import common.battle.data.OrbInfo;
import common.battle.entity.Entity;
import common.io.json.JsonClass;
import common.io.json.JsonDecoder;
import common.io.json.LocalDecoder;
import common.io.json.JsonField;
import common.pack.*;
import common.pack.IndexContainer.Indexable;
import common.system.VImg;
import common.util.Data;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@IndexContainer.IndexCont(PackData.class)
@JsonClass.JCGeneric(Identifier.class)
@JsonClass
public class Trait extends Data implements Indexable<PackData, Trait>, Comparable<Trait> {
    public static void read() {
        //Reads traits from BC and implements it into the main pack
        PackData.DefPack data = UserProfile.getBCData();
        String[] traitNames = {"Red", "Floating", "Black", "Metal", "Angel", "Alien", "Zombie", "Aku", "Relic", "White", "EVA", "Witch", "Baron", "Beast", "Sage", "base", "cannon"};
        for (String name : traitNames) {
            Trait t = new Trait(data.getNextID(Trait.class));
            t.name = name;
            data.traits.add(t);
        }
    }
    // Convert Bitmask Type format to new format
    public static SortedPackSet<Trait> convertType(int type, boolean talent) {
        SortedPackSet<Trait> traits = new SortedPackSet<>();
        PackData.DefPack data = UserProfile.getBCData();
        if ((type & TB_RED) != 0)
            traits.add(data.traits.get(TRAIT_RED));
        if ((type & TB_FLOAT) != 0)
            traits.add(data.traits.get(TRAIT_FLOAT));
        if ((type & TB_BLACK) != 0)
            traits.add(data.traits.get(TRAIT_BLACK));
        if ((type & TB_METAL) != 0)
            traits.add(data.traits.get(TRAIT_METAL));
        if ((type & TB_ANGEL) != 0)
            traits.add(data.traits.get(TRAIT_ANGEL));
        if ((type & TB_ALIEN) != 0)
            traits.add(data.traits.get(TRAIT_ALIEN));
        if ((type & TB_ZOMBIE) != 0)
            traits.add(data.traits.get(TRAIT_ZOMBIE));
        if ((type & TB_RELIC) != 0)
            traits.add(data.traits.get(TRAIT_RELIC));
        if ((type & TB_WHITE) != 0)
            traits.add(data.traits.get(TRAIT_WHITE));
        if ((type & TB_EVA) != 0)
            traits.add(data.traits.get(TRAIT_EVA));
        if ((type & TB_WITCH) != 0)
            traits.add(data.traits.get(TRAIT_WITCH));
        if (talent) {
            if ((type & TB_DEMON_T) != 0)
                traits.add(data.traits.get(TRAIT_DEMON));
        } else {
            if ((type & TB_DEMON) != 0)
                traits.add(data.traits.get(TRAIT_DEMON));
            if ((type & TB_INFH) != 0)
                traits.add(data.traits.get(TRAIT_INFH));
        }
        return traits;
    }

    public static List<Trait> convertOrb(int mask) {
        List<Trait> ans = new ArrayList<>();
        PackData.DefPack data = UserProfile.getBCData();

        for (int i = 0; i < OrbInfo.orbTrait.length; i++)
            if ((mask & (1 << OrbInfo.orbTrait[i])) > 0)
                ans.add(data.traits.get(OrbInfo.orbTrait[i]));

        return ans;
    }

    public static boolean isUsed(Trait t) {
        return t.isUsed();
    }

    @JsonField(defval = "new trait")
    public String name = "new trait";

    @JsonClass.JCIdentifier
    @JsonField
    public Identifier<Trait> id;
    public VImg icon = null;

    @JsonField
    public boolean targetType;
    // Target type will be used to toggle whether Anti-Traited, Anti-Non Metal, or Anti-All units will target this trait or not

    @JsonField(generic = Form.class, alias = AbForm.AbFormJson.class, defval = "isEmpty")
    public final SortedPackSet<Form> targetForms = new SortedPackSet<>();
    // This is used to make custom traits targeted by units whose stats can't be modified otherwise, such as BC units or units from Parented Packs


    @JsonClass.JCConstructor
    public Trait() {
        id = null;
    }

    public Trait(Identifier<Trait> id, Trait t) {
        name = t.name;
        targetType = t.targetType;
        this.id = id;
        icon = t.icon;
        targetForms.addAll(t.targetForms);
    }

    public Trait(Identifier<Trait> id) {
        this.id = id;
    }

    @Override
    public Identifier<Trait> getID() { return id; }

    @Override
    public String toString() {
        return id + " - " + name;
    }

    public boolean isUsed() {
        if (BCTrait())
            return true;
        PackData.UserPack pack = (PackData.UserPack) getCont();
        Collection<PackData.UserPack> pacs = UserProfile.getUserPacks();
        for (PackData.UserPack pacc : pacs)
            if (pacc.desc.dependency.contains(pack.desc.id) || pacc.desc.id.equals(pack.desc.id)) {
                for (Enemy en : pacc.enemies.getList())
                    if (en.de.getTraits(false).contains(this))
                        return true;
                for (Unit un : pacc.units.getList())
                    for (Form uf : un.forms)
                        if (uf.du.getTraits(false).contains(this))
                            return true;
            }
        return false;
    }

    @JsonDecoder.OnInjected
    public void onInjected(JsonObject jobj) {
        icon = UserProfile.getUserPack(id.pack).source.readImage(Source.BasePath.TRAIT.toString(), id.id);
        if (jobj.has("others"))
            targetForms.addAll(new LocalDecoder(jobj.get("others"), SortedPackSet.class, this).setGeneric(Form.class).setAlias(AbForm.AbFormJson.class).decode());
        targetForms.removeIf(f -> f == null || f.uid.pack.equals(id.pack) || f.maxu().getTraits(true).isEmpty() || (targetType && Entity.targetTraited(f.maxu().getTraits(true))));
    }

    @Override
    public int compareTo(@NotNull Trait t) {
        return id.compareTo(t.id);
    }

    public boolean BCTrait() {
        return id.pack.equals(Identifier.DEF);
    }
}