package common.battle.entity;

import common.CommonStatic;
import common.battle.attack.AttackAb;
import common.util.pack.EffAnim;

public class DemonCont extends EAnimCont {

    private final Entity ent;
    private final Proc.ProcItem volc;

    public DemonCont(Entity e, AttackAb atk) {
        super(e.pos, e.layer, (e.dire == -1 ? effas().A_DEMONVOLC : effas().A_E_DEMONVOLC).getEAnim(EffAnim.DefEff.DEF));
        if ((atk.waveType & WT_VOLC) > 0)
            volc = atk.getProc().VOLC;
        else
            volc = atk.getProc().MINIVOLC;
        ent = e;
        e.basis.lea.add(this);
    }

    @Override
    public void update() {
        super.update();
        if (getAnim().ind() == 43)
            ent.aam.getCounterSurge(pos, volc);
        else if (getAnim().ind() == 18)
            CommonStatic.setSE(SE_COUNTERVOLC);
    }
}
