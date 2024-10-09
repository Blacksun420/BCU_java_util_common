package common.battle.attack;

import common.CommonStatic;
import common.system.P;
import common.system.fake.FakeGraphics;
import common.system.fake.FakeTransform;
import common.util.anim.EAnimD;
import common.util.pack.EffAnim;

import java.util.HashSet;
import java.util.Set;

public class ContBlast extends ContAb {
    protected final EAnimD<EffAnim.BlastEff> anim;
    //protected Set<ContBlast> blasts;
    private final AttackBlast blast;
    private int t = 0;

    protected ContBlast(AttackBlast atkBlast, float p, int lay) {
        super(atkBlast.model.b, p, lay);
        anim = (atkBlast.dire == 1 ? effas().A_E_BLAST : effas().A_BLAST).getEAnim(EffAnim.BlastEff.START);
        anim.setTime(1);
        blast = atkBlast;
        //if (atkBlast.getProc().BLAST.lv != 3)
        //    blasts = new HashSet<>();
    }
    /*private ContBlast(ContBlast blast, float p, int lay) {
        super(blast.sb, p, lay);
        anim = (blast. == 1 ? effas().A_E_BLAST : effas().A_BLAST).getEAnim(EffAnim.BlastEff.SINGLE);
        anim.setTime(1);
        if (atkBlast.getProc().BLAST.lv != 3) {
            blasts = new HashSet<>();
        }
    }*/

    @Override
    public void draw(FakeGraphics gra, P p, float psiz) {
        FakeTransform at = gra.getTransform();
        anim.draw(gra, p, psiz);
        gra.setTransform(at);
        if (CommonStatic.getConfig().ref)
            drawAxis(gra, p, psiz * 1.25f);
    }

    public void drawAxis(FakeGraphics gra, P p, float siz) {
        float rat = CommonStatic.BattleConst.ratio;
        int h = (int) (640 * rat * siz);
        gra.setColor(FakeGraphics.MAGENTA);

        float d0 = Math.min(blast.sta, blast.end);
        float ra = Math.abs(blast.sta - blast.end);
        int x = (int) ((d0 - pos) * rat * siz + p.x);
        int y = (int) p.y;
        int w = (int) (ra * rat * siz);

        int ch = (int)(150 * Math.max(0, blast.lv) * rat * siz);
        if (t > EXPLOSION_PRE && (t - EXPLOSION_PRE) % 10 == 2) {
            gra.fillRect(x + ch, y, w, h);
            gra.fillRect(x - ch, y, w, h);
        } else {
            gra.drawRect(x + ch, y, w, h);
            gra.drawRect(x - ch, y, w, h);
        }
    }

    @Override
    public void update() { // FIXME: update on same frame as attack
        t++;

        if (t == EXPLOSION_PRE)
            anim.changeAnim(EffAnim.BlastEff.EXPLODE, true);
        if (t > EXPLOSION_PRE && (t - 1) % 10 == 0)
            CommonStatic.setSE(EXPLOSION_SE + ((t - EXPLOSION_PRE) / 10));
        if (t > EXPLOSION_PRE && (t - 1) % 10 == 2)
            blast.capture();
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
