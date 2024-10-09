package common.battle.attack;

import common.battle.entity.AbEntity;
import common.battle.entity.Entity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AttackBlast extends AttackAb {

    private final int reduction;
    public int lv = -1;
    public int raw;

    protected AttackBlast(Entity attacker, AttackSimple src, float pos, int bt) {
        super(attacker, src, pos + 75, pos - 75, false);
        waveType = bt;
        reduction = src.getProc().BLAST.reduction;
        raw = ((AtkModelEntity)model).getDefAtk(matk);
    }

    @Override
    public void capture() {
        lv++;
        capt.clear();
        float rng = (150 * lv);

        List<AbEntity> le = model.b.inRange(touch, attacker != null && attacker.status.rage > 0 ? 2 : dire, sta + rng, end + rng, excludeRightEdge);
        if (lv > 0) {
            List<AbEntity> nle = model.b.inRange(touch, attacker != null && attacker.status.rage > 0 ? 2 : dire, sta - rng, end - rng, excludeRightEdge);
            nle.removeIf(le::contains);
            le.addAll(nle);
        }
        if ((abi & AB_ONLY) == 0)
            capt.addAll(le);
        else
            for (AbEntity e : le)
                if (e.ctargetable(trait, attacker))
                    capt.add(e);
        excuse();
    }

    @Override
    public void excuse() {
        atk = ((AtkModelEntity)model).getEffMult(raw);
        atk -= (lv * reduction / 100) * atk;

        for (AbEntity e : capt)
            e.damaged(this);
    }
}
