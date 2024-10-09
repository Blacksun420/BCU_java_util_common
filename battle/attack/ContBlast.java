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
        //    blasts = new HashSet<>(); TODO - levels other than 3
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
        if (CommonStatic.getConfig().ref && blast.lv + 1 < blast.getProc().BLAST.lv)
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

        int ch = (int)(150 * blast.lv * rat * siz);
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
        int rt = t - EXPLOSION_PRE;
        if (rt >= 0 && blast.lv + 1 < blast.getProc().BLAST.lv) {
            if (rt == 0)
                anim.changeAnim(EffAnim.BlastEff.EXPLODE, true);
            int qrt = (t - 1) % EXPLOSION_DELAY;
            if (qrt == 0)
                CommonStatic.setSE(EXPLOSION_SE + (rt / 10));
            else if (qrt == 2)
                blast.capture();
            if (qrt == 9)
                blast.lv++;
        }
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
