package common.util.pack.bgeffect;

import common.io.json.JsonClass;
import common.io.json.JsonField;
import common.pack.Identifier;
import common.system.P;
import common.system.fake.FakeGraphics;
import common.util.Animable;
import common.util.anim.AnimU;
import common.util.anim.EAnimI;
import common.util.pack.Background;

@JsonClass.JCGeneric(BackgroundEffect.BGIdentifier.class)
@JsonClass
public class CustomBGEffect extends Animable<AnimU<?>, AnimU.UType> implements BackgroundEffect {

    @JsonClass.JCIdentifier
    @JsonField
    public Identifier<BackgroundEffect> id;
    @JsonField
    public String name;
    @JsonField
    public int spacer = 0; //Redraw the background for each dist specified by this, unless it's -1;

    @JsonClass.JCConstructor
    public CustomBGEffect() {
        id = null;
    }

    public CustomBGEffect(Identifier<BackgroundEffect> id, AnimU<?> abg) {
        this.id = id;
        name = "BGEffect " + id;
        anim = abg;
    }

    @Override
    public void check() {
        anim.anim.load();
    }

    @Override
    public void preDraw(FakeGraphics g, P rect, double siz, double midH) {
        anim.getEAnim(AnimU.UType.BACKGROUND).draw(g, rect, siz);
    }

    @Override
    public void postDraw(FakeGraphics g, P rect, double siz, double midH) {
        anim.getEAnim(AnimU.UType.FOREGROUND).draw(g, rect, siz);
    }

    @Override
    public void draw(FakeGraphics g, double x, double y, double siz, int groundH, int skyH) {
        anim.getEAnim(AnimU.UType.BACKGROUND).draw(g, P.newP(x, y), siz);
        anim.getEAnim(AnimU.UType.FOREGROUND).draw(g, P.newP(x, y), siz);
    }

    @Override
    public void update(int w, double h, double midH) {
        anim.getEAnim(AnimU.UType.BACKGROUND).update(false);
        anim.getEAnim(AnimU.UType.FOREGROUND).update(false);
    }

    @Override
    public void initialize(int w, double h, double midH, Background bg) {
        anim.getEAnim(AnimU.UType.BACKGROUND).setTime(0);
        anim.getEAnim(AnimU.UType.FOREGROUND).setTime(0);
    }

    @Override
    public Identifier<BackgroundEffect> getID() {
        return id;
    }

    @Override
    public String toString() {
        if (getName().length() == 0)
            return id.toString();
        return id + " - " + getName();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public EAnimI getEAnim(AnimU.UType ut) {
        return anim.getEAnim(ut);
    }
}
