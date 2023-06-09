package common.battle.attack;

import common.battle.entity.AbEntity;
import common.battle.entity.Entity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AttackWave extends AttackAb {

	protected final Set<Entity> incl;
	public int raw;

	public AttackWave(Entity e, AttackSimple a, double p0, double wid, int wt) {
		super(e, a, p0 - wid / 2, p0 + wid / 2, false);
		incl = new HashSet<>();
		waveType = wt;
		if(wt != WT_MOVE && dire == 1 && model.b.canon.deco == DECO_BASE_WALL)
			atk *= model.b.b.t().getDecorationMagnification(model.b.canon.deco);
		raw = atk;
	}

	public AttackWave(Entity e, AttackWave a, double p0, double wid) {
		super(e, a, p0 - wid / 2, p0 + wid / 2, false);
		incl = a.incl;
		waveType = a.waveType;
		raw = atk;
	}

	public AttackWave(Entity e, AttackWave a, double pos, double start, double end) {
		super(e, a, pos - start, pos + end, false);
		incl = a.incl;
		waveType = a.waveType;
		raw = atk;
	}

	@Override
	public void capture() {
		List<AbEntity> le = model.b.inRange(touch, attacker != null && attacker.status.rage > 0 ? 2 : dire, sta, end, excludeLastEdge);
		le.remove(dire == 1 ? model.b.ubase : model.b.ebase);
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

		if(attacker != null)
			atk = ((AtkModelEntity)model).getEffMult(raw);
		for (AbEntity e : capt) {
			if (e instanceof Entity) {
				e.damaged(this);
				incl.add((Entity) e);
			}
		}
		r.clear();
	}
}
