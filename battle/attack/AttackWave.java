package common.battle.attack;

import common.battle.entity.AbEntity;
import common.battle.entity.Entity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AttackWave extends AttackAb {

	protected final Set<Entity> incl;

	public AttackWave(Entity e, AttackSimple a, double p0, double wid, int wt) {
		super(e, a, p0 - wid / 2, p0 + wid / 2, false);
		waveType = wt;
		isCounter = a.isCounter;
		incl = new HashSet<>();
	}

	public AttackWave(Entity e, AttackWave a, double p0, double wid) {
		super(e, a, p0 - wid / 2, p0 + wid / 2, false);
		waveType = a.waveType;
		isCounter = a.isCounter;
		incl = a.incl;
	}

	public AttackWave(Entity e, AttackWave a, double pos, double start, double end) {
		super(e, a, pos - start, pos + end, false);
		waveType = a.waveType;
		isCounter = a.isCounter;
		incl = a.incl;
	}

	@Override
	public void capture() {
		List<AbEntity> le = model.b.inRange(touch, attacker != null && attacker.status.rage > 0 ? 2 : dire, sta, end, excludeLastEdge);
		if (incl != null)
			le.removeIf(incl::contains);
		capt.clear();
		if ((abi & AB_ONLY) == 0)
			capt.addAll(le);
		else
			for (AbEntity e : le)
				if (e.ctargetable(trait, attacker))
					capt.add(e);
	}

	@Override
	public void excuse() {
		process();

		if(attacker != null) {
			atk = ((AtkModelEntity)model).getEffAtk(matk);
		}

		for (AbEntity e : capt) {
			if (e.isBase())
				continue;

			if (e instanceof Entity) {
				e.damaged(this);
				incl.add((Entity) e);
			}
		}
		r.clear();
	}
}
