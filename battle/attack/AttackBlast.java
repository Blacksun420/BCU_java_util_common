package common.battle.attack;

import common.battle.entity.Entity;

import java.util.HashSet;
import java.util.Set;

public class AttackBlast extends AttackAb {
    protected final Set<Entity> captured = new HashSet<>();

    protected AttackBlast(Entity attacker, AttackSimple src, int bt) {
        super(attacker, src, 75, -75, false);
        waveType = bt;
    }

    @Override
    public void capture() {

    }

    @Override
    public void excuse() {

    }
}
