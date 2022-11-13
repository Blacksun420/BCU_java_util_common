package common.battle.entity;

import common.battle.StageBasis;
import common.system.P;
import common.system.fake.FakeGraphics;
import common.system.fake.FakeTransform;
import common.util.anim.AnimU;
import common.util.pack.EffAnim;

import java.util.Comparator;

public class DoorCont extends EAnimCont {

    private final StageBasis bas;
    private final Entity ent;
    private boolean entLeft;

    public DoorCont(StageBasis b, Entity e) {
        super(e.pos, e.layer, effas().A_DOOR.getEAnim(EffAnim.DefEff.DEF));
        bas = b;
        ent = e;

        ent.getAnim().ent[0].EWarp = true;
        ent.getAnim().paraTo(getAnim(), 24);
    }

    @Override
    public void draw(FakeGraphics gra, P p, double psiz) {
        FakeTransform at = gra.getTransform();
        super.draw(gra, p, psiz);
        gra.setTransform(at);
        if (!entLeft)
            ent.getAnim().draw(gra, p, psiz);
        gra.delete(at);
    }

    @Override
    public void update() {
        super.update();
        if (getAnim().ind() > 9) {
            if (getAnim().ind() < 18) {
                if (!ent.getAnim().anim().getEAnim(AnimU.TYPEDEF[AnimU.ENTRY]).unusable()) {
                    ent.getAnim().update(false);
                }
            } else if (!entLeft) {
                ent.getAnim().paraTo(null);
                bas.le.add(ent);
                bas.le.sort(Comparator.comparingInt(en -> en.layer));
                entLeft = true;
            }
        }
    }
}
