package common.util.pack.bgeffect;

import common.CommonStatic;
import common.io.json.JsonClass;
import common.io.json.JsonField;
import common.pack.Identifier;
import common.system.P;
import common.system.fake.FakeGraphics;
import common.util.Data;
import common.util.pack.Background;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@JsonClass.JCGeneric(Identifier.class)
@JsonClass
@SuppressWarnings("ForLoopReplaceableByForEach")
public class MixedBGEffect extends BackgroundEffect {

    @JsonField
    public String name;
    @JsonField(generic = BackgroundEffect.class, alias = Identifier.class)
    public final ArrayList<BackgroundEffect> effects = new ArrayList<>();

    @JsonClass.JCConstructor
    public MixedBGEffect() {
        super(null);
    }

    public MixedBGEffect(Identifier<BackgroundEffect> id, BackgroundEffect... effects) {
        super(id);
        name = "BGEffect " + id;
        this.effects.addAll(Arrays.asList(effects));
    }

    public MixedBGEffect(Identifier<BackgroundEffect> id, List<BackgroundEffect> effects) {
        super(id);
        name = "BGEffect " + id;
        this.effects.addAll(effects);
    }

    @Override
    public void check() {
    }

    @Override
    public void preDraw(FakeGraphics g, P rect, double siz, double midH) {
        for(int i = 0; i < effects.size(); i++)
            effects.get(i).preDraw(g, rect, siz, midH);
    }

    @Override
    public void postDraw(FakeGraphics g, P rect, double siz, double midH) {
        for(int i = 0; i < effects.size(); i++)
            effects.get(i).postDraw(g, rect, siz, midH);
    }

    @Override
    public void draw(FakeGraphics g, double y, double siz, double midH) {
        for (int i = 0; i < effects.size(); i++)
            effects.get(i).draw(g, y, siz, midH);
    }

    @Override
    public void update(int w, double h, double midH) {
        for(int i = 0; i < effects.size(); i++)
            effects.get(i).update(w, h, midH);
    }

    @Override
    public void initialize(int w, double h, double midH, Background bg) {
        for(int i = 0; i < effects.size(); i++)
            effects.get(i).initialize(w, h, midH, bg);
    }

    @Override
    public void release() {
        for(int i = 0; i < effects.size(); i++)
            effects.get(i).release();
    }

    @Override
    public String toString() {
        if (id.pack.equals(Identifier.DEF)) {
            if (id.id == Data.BG_EFFECT_SNOWSTAR)
                return CommonStatic.def.getBtnName(0, "bgeff5");


            String temp = CommonStatic.def.getBtnName(0, "bgjson" + id.id);
            if (temp.equals("bgjson" + id.id))
                temp = CommonStatic.def.getBtnName(0, "bgeffdum").replace("_", "" + id.id);
            return temp;
        }
        if (getName().length() == 0)
            return id.toString();
        return Data.trio(id.id) + " - " + getName();
    }

    @Override
    public String getName() {
        return name;
    }
}
