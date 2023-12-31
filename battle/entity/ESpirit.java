package common.battle.entity;

import common.battle.StageBasis;
import common.battle.attack.AttackAb;
import common.battle.data.MaskAtk;
import common.battle.data.MaskUnit;
import common.util.anim.EAnimD;
import common.util.anim.EAnimU;
import common.util.pack.EffAnim.DefEff;
import common.util.unit.Level;

public class ESpirit extends EUnit {

    private EAnimD<DefEff> imuAnim = null;

    public ESpirit(StageBasis b, MaskUnit de, EAnimU ea, float lvd, int layer, Level lv, int[] ind) {
        super(b, de, ea, lvd, layer, layer, lv, null, ind, false);
        auras.updateAuras();
        atkm.setUpAtk(false);
    }

    @Override
    public float calcDamageMult(int dmg, Entity e, MaskAtk matk) {
        return 0;
    }

    @Override
    public void damaged(AttackAb atk) {
        if (imuAnim == null)
            imuAnim = effas().A_IMUATK.getEAnim(DefEff.DEF);
    }

    @Override
    public int getAbi() {
        return data.getAbi() | AB_IMUSW;
    }

    @Override
    public void preUpdate() {
    }

    @Override
    public void update() {
        // update attack status when in attack state
        if (atkm.atkTime > 1)
            atkm.updateAttack();
        else
            atkm.atkTime--;
        updateAnimation();
    }

    @Override
    public void updateAnimation() {
        if (imuAnim != null)
            imuAnim.update(false);
        super.updateAnimation();
    }

    @Override
    public void postUpdate() {
        if ((getAbi() & AB_GLASS) > 0 && atkm.atkTime == 0 && atkm.loop == 0)
            kill(true);

        if(!dead || !summoned.isEmpty())
            livingTime++;
        summoned.removeIf(s -> !s.activate);
        acted = false;
    }

    @Override
    public int touchable() {
        return TCH_N;
    }
}
