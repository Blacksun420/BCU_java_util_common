package common.battle.entity;

import common.CommonStatic;
import common.battle.attack.AttackVolcano;
import common.util.pack.EffAnim;

public class DemonCont extends EAnimCont {

    private final Entity ent;
    private final Proc.ProcItem volc;

    public DemonCont(Entity e, AttackVolcano atk) {
        super(e.pos, e.layer, (e.dire == -1 ? effas().A_COUNTERSURGE : effas().A_E_COUNTERSURGE).getEAnim(EffAnim.DefEff.DEF));
        if ((atk.waveType & WT_VOLC) > 0)
            volc = atk.handler.ds ? atk.attacker.getProc().DEATHSURGE : atk.getProc().VOLC;
        else
            volc = atk.getProc().MINIVOLC;
        ent = e;
        e.basis.lea.add(this);
    }

    @Override
    public void update() {
        super.update();
        if (getAnim().ind() == COUNTER_SURGE_FORESWING)
            ent.aam.getCounterSurge(pos, volc);
        else if (getAnim().ind() == COUNTER_SURGE_SOUND)
            CommonStatic.setSE(SE_COUNTER_SURGE);
    }
}
