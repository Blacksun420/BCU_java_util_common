package common.battle.attack;

import common.CommonStatic;
import common.system.P;
import common.system.fake.FakeGraphics;
import common.system.fake.FakeTransform;
import common.util.anim.EAnimD;
import common.util.pack.EffAnim;

public class ContBlast extends ContAb {
    protected final EAnimD<EffAnim.BlastEff> anim;
    private int t = 0;

    protected ContBlast(AttackBlast atkBlast, float p, int lay) {
        super(atkBlast.model.b, p, lay);
        anim = (atkBlast.dire == 1 ? effas().A_E_BLAST : effas().A_BLAST).getEAnim(EffAnim.BlastEff.START);
        anim.setTime(1);
    }

    @Override
    public void draw(FakeGraphics gra, P p, float psiz) {
        FakeTransform at = gra.getTransform();
        anim.draw(gra, p, psiz);
        gra.setTransform(at);
//        if (CommonStatic.getConfig().ref)
//            drawAxis(gra, p, psiz * 1.25f);
    }

    public void drawAxis(FakeGraphics gra, P p, float siz) {
        float rat = CommonStatic.BattleConst.ratio;
        int h = (int) (640 * rat * siz);
        gra.setColor(FakeGraphics.MAGENTA);
    }

    @Override
    public void update() { // FIXME: update on same frame as attack
        t++;
        System.out.println("battle frame " + sb.time + "f, blast frame " + t + "f");

        if (t == EXPLOSION_PRE)
            anim.changeAnim(EffAnim.BlastEff.EXPLODE, true);
        if (t > 1 && (t - 1) % 10 == 0)
            CommonStatic.setSE(EXPLOSION_SE + (t / 11 - 1));
        if (anim.done())
            activate = false;
        updateAnimation();
    }

    @Override
    public void updateAnimation() {
        anim.update(false);
    }

    @Override
    public boolean IMUTime() {
        return false;
    }
}
