package common.util.pack.bgeffect;

import common.io.json.JsonClass;
import common.io.json.JsonField;
import common.pack.Identifier;
import common.system.BattleRange;
import common.system.P;
import common.system.fake.FakeGraphics;
import common.system.fake.FakeTransform;
import common.util.Animable;
import common.util.Data;
import common.util.anim.*;
import common.util.pack.Background;

@JsonClass.JCGeneric(BackgroundEffect.BGIdentifier.class)
@JsonClass
public class CustomBGEffect extends Animable<AnimU<?>, AnimU.UType> implements BackgroundEffect {

    private static final P origin = new P(0, 0);
    @JsonClass.JCIdentifier
    @JsonField
    public Identifier<BackgroundEffect> id;
    @JsonField
    public String name;
    @JsonField
    public int spacer = 0, fspacer = 0; //Redraw the background for each dist specified by this, unless it's -1;
    private boolean loaded = false;

    public final EAnimU[] ebg = new EAnimU[2];


    @JsonClass.JCConstructor
    public CustomBGEffect() {
        id = null;
    }

    public CustomBGEffect(Identifier<BackgroundEffect> id, AnimCE abg) {
        this.id = id;
        name = "BGEffect " + id;
        anim = abg;
        ebg[0] = anim.getEAnim(AnimU.BGEFFECT[0]);
        ebg[1] = anim.getEAnim(AnimU.BGEFFECT[1]);
        loaded = true;
    }

    @Override
    public void check() {
        if (!loaded) {
            anim.anim.load();
            ebg[0] = anim.getEAnim(AnimU.BGEFFECT[0]);
            ebg[1] = anim.getEAnim(AnimU.BGEFFECT[1]);
            loaded = true;
        }
    }
    @Override
    public void preDraw(FakeGraphics g, P rect, double siz, double midH) {
        FakeTransform at = g.getTransform();
        g.translate(convertP(1024, siz) + rect.x, convertP(7000 - midH, siz) - rect.y);
        ebg[0].drawBGEffect(g, origin, siz * 0.8, 1000, 1, 1);
        g.setTransform(at);
        g.delete(at);
    }

    @Override
    public void postDraw(FakeGraphics g, P rect, double siz, double midH) {
        FakeTransform at = g.getTransform();
        g.translate(convertP(1024, siz) + rect.x, convertP(7000 - midH, siz) - rect.y);
        ebg[1].drawBGEffect(g, origin, siz * 0.8, 255, 1, 1);
        g.setTransform(at);
        g.delete(at);
    }

    @Override
    public void draw(FakeGraphics g, double x, double y, double siz, int groundH, int skyH) {
        siz *= 0.8;
        FakeTransform at = g.getTransform();
        g.translate(convertP(1024, siz) + x, convertP(7000 - skyH, siz) - y);
        ebg[0].draw(g, P.newP(x, y), siz);
        ebg[1].draw(g, P.newP(x, y), siz);
        g.setTransform(at);
        g.delete(at);
    }

    @Override
    public void update(int w, double h, double midH) {
        check();
        ebg[0].update(false);
        ebg[1].update(false);
    }

    @Override
    public void initialize(int w, double h, double midH, Background bg) {
        check();
        ebg[0].setTime(0);
        ebg[1].setTime(0);
    }

    /**
     * Convert battle unit to pixel unit
     * @param p Position in battle
     * @param siz Size of battle
     * @return Converted pixel
     */
    private static int convertP(double p, double siz) {
        return (int) (p * BattleRange.battleRatio * siz);
    }

    @Override
    public Identifier<BackgroundEffect> getID() {
        return id;
    }

    @Override
    public String toString() {
        if (getName().length() == 0)
            return id.toString();
        return Data.trio(id.id) + " - " + getName();
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
