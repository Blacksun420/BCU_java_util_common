package common.battle.attack;

import common.battle.entity.AbEntity;
import common.battle.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class AttackVolcano extends AttackAb {

	protected boolean attacked = false;
	private byte volcTime = VOLC_ITV;

	protected final List<Entity> vcapt = new ArrayList<>();

	public AttackVolcano(Entity e, AttackSimple a, double sta, double end, int vt) {
		super(e, a, sta, end, false);
		this.sta = sta;
		this.end = end;
		this.waveType = vt;

		if(dire == 1 && model.b.canon.deco == DECO_BASE_WATER)
			atk *= model.b.b.t().getDecorationMagnification(model.b.canon.deco);
	}

	@Override
	public void capture() {
		List<AbEntity> le = model.b.inRange(touch, attacker.status.rage > 0 ? 2 : dire, sta, end, excludeLastEdge);
		capt.clear();

		for (AbEntity e : le)
			if (e instanceof Entity && !vcapt.contains((Entity) e) && ((abi & AB_ONLY) == 0 || e.isBase() || e.ctargetable(trait, attacker)))
				capt.add(e);
	}

	@Override
	public void excuse() {
		process();

		if (volcTime == 0) {
			volcTime = VOLC_ITV;
			vcapt.clear();
		} else
			volcTime--;

		if(attacker != null)
			atk = ((AtkModelEntity)model).getEffAtk(matk);

		for (AbEntity e : capt) {
			e.damaged(this);
			vcapt.add((Entity) e);
		}
		attacked = capt.size() > 0;
		r.clear();
	}
}
