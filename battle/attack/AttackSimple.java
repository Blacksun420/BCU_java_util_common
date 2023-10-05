package common.battle.attack;

import common.CommonStatic;
import common.battle.data.MaskAtk;
import common.battle.entity.AbEntity;
import common.battle.entity.Entity;
import common.battle.entity.Sniper;
import common.pack.Identifier;
import common.pack.SortedPackSet;
import common.util.Data.Proc.MOVEWAVE;
import common.util.Data.Proc.VOLC;
import common.util.stage.Music;
import common.util.unit.Trait;

import java.util.*;

public class AttackSimple extends AttackAb {

	/**
	 * avoid attacking already attacked enemies for lasting attacks
	 */
	private final Set<AbEntity> attacked = new HashSet<>();
	public final boolean range;

	public AttackSimple(Entity attacker, AtkModelAb ent, int ATK, SortedPackSet<Trait> tr, int eab, Proc pro, float p0, float p1, boolean isr,
						MaskAtk matk, int layer, boolean isLongAtk, int duration) {
		super(attacker, ent, ATK, tr, eab, pro, p0, p1, matk, layer, isLongAtk, duration);
		range = isr;
	}

	public AttackSimple(Entity attacker, AtkModelAb ent, int ATK, SortedPackSet<Trait> tr, int eab, Proc proc, float p0, float p1, MaskAtk mask, int layer, boolean isLongAtk) {
		this(attacker, ent, ATK, tr, eab, proc, p0, p1, mask, layer, isLongAtk, mask.isRange());
	}

	public AttackSimple(Entity attacker, AtkModelAb ent, int ATK, SortedPackSet<Trait> tr, int eab, Proc proc, float p0, float p1, MaskAtk mask, int layer, boolean isLongAtk, boolean isRange) {
		this(attacker, ent, ATK, tr, eab, proc, p0, p1, isRange, mask, layer, isLongAtk, 1);
		touch = mask.getTarget();

		if((eab & AB_CKILL) > 0)
			touch |= TCH_CORPSE;

		dire *= mask.getDire();
	}

	@Override
	public void capture() {
		float pos = model.getPos();
		List<AbEntity> le = model.b.inRange(touch, attacker != null && attacker.status.rage > 0 ? 2 : dire, sta, end, excludeLastEdge);
		if (attacker != null && (attacker.status.rage > 0 || attacker.status.hypno > 0))
			le.remove(attacker);

		if(attacker != null && isLongAtk && !le.contains(model.b.getBase(attacker.dire))) {
			if(attacker.dire == -1 && dire == -1 && sta <= model.b.getBase(attacker.dire).pos)
				le.add(model.b.getBase(attacker.dire));
			else if (attacker.dire == 1 && dire == 1 && sta >= model.b.getBase(attacker.dire).pos)
				le.add(model.b.getBase(attacker.dire));
		}
		le.removeIf(attacked::contains);
		capt.clear();
		if (canon > -2 || model instanceof Sniper)
			le.remove(model.b.ebase);
		if ((abi & AB_ONLY) == 0)
			capt.addAll(le);
		else
			for (AbEntity e : le)
				if (e.isBase() || e.ctargetable(trait, attacker))
					capt.add(e);
		if (!range) {
			if (capt.size() == 0)
				return;
			List<AbEntity> ents = new ArrayList<>();
			ents.add(capt.get(0));
			float dis = Math.abs(pos - ents.get(0).pos);
			for (AbEntity e : capt)
				if (Math.abs(pos - e.pos) < dis - 0.1) {
					ents.clear();
					ents.add(e);
					dis = Math.abs(pos - e.pos);
				} else if (Math.abs(pos - e.pos) < dis + 0.1)
					ents.add(e);
			capt.clear();
			int r = (int) (model.b.r.nextFloat() * ents.size());
			capt.add(ents.get(r));
		} else
			capt.sort(Comparator.comparingInt(e -> -e.getProc().REMOTESHIELD.prob));
	}

	/**
	 * Method to manually add an unit to an attack for counters.
	 */
	public boolean counterEntity(Entity ce) {
		isCounter = true;
		if (ce != null && !capt.contains(ce))
			capt.add(ce);
		excuse();
		return capt.size() > 0;
	}

	@Override
	public void excuse() {
		process();
		int layer = model.getLayer();
		if (proc.MOVEWAVE.exists()) {
			MOVEWAVE mw = proc.MOVEWAVE;
			int dire = model.getDire();
			float p0 = model.getPos() + dire * mw.dis;
			new ContMove(this, p0, mw.width, mw.speed, 1, mw.time, mw.itv, layer);
			return;
		}
		for (AbEntity e : capt) {
			e.damaged(this);
			attacked.add(e);
		}
		r.clear();
		if (dire == 0) {
			Identifier<Music> sfx0 = matk.getAudio(false);
			Identifier<Music> sfx1 = matk.getAudio(true);
			boolean b = sfx1 == null || attacker.basis.r.irDouble() < 0.5;
			if (sfx0 != null && b)
				CommonStatic.setSE(sfx0);
			else if (!b)
				CommonStatic.setSE(sfx1);
		}
		if (proc.WAVE.exists() && (capt.size() > 0 || proc.WAVE.type.hitless)) {
			int dire = model.getDire();
			int wid = dire == 1 ? W_E_WID : W_U_WID;
			float addp = (dire == 1 ? W_E_INI : W_U_INI) + wid / 2f;
			float p0 = model.getPos() + dire * addp;
			// generate a wave when hits somebody

			ContWaveDef wave = new ContWaveDef(new AttackWave(attacker, this, p0, wid, WT_WAVE), p0, layer, true);

			if(attacker != null)
				attacker.summoned.add(wave);
		}
		if(proc.MINIWAVE.exists() && (capt.size() > 0 || proc.MINIWAVE.type.hitless)) {
			int dire = model.getDire();
			int wid = dire == 1 ? W_E_WID : W_U_WID;
			float addp = (dire == 1 ? W_E_INI : W_U_INI) + wid / 2f;
			float p0 = model.getPos() + dire * addp;

			ContWaveDef wave = new ContWaveDef(new AttackWave(attacker, this, p0, wid, proc.MINIWAVE.multi > 100 ? WT_MEGA : WT_MINI), p0, layer, proc.MINIWAVE.multi > 100);
			wave.atk.raw *= proc.MINIWAVE.multi / 100.0;

			if(attacker != null)
				attacker.summoned.add(wave);
		}
		if (proc.VOLC.exists() && (capt.size() > 0 || proc.VOLC.type.hitless)) {
			int dire = model.getDire();
			VOLC volc = proc.VOLC;
			int addp = volc.dis_0 == volc.dis_1 ? volc.dis_0 : volc.dis_0 + (int) (model.b.r.nextFloat() * (volc.dis_1 - volc.dis_0));
			float p0 = model.getPos() + dire * addp;
			float sta = p0 + (dire == 1 ? W_VOLC_PIERCE : W_VOLC_INNER);
			float end = p0 - (dire == 1 ? W_VOLC_INNER : W_VOLC_PIERCE);

			ContVolcano volcano = new ContVolcano(new AttackVolcano(attacker, this, sta, end, WT_VOLC), p0, layer, volc.time, false);
			if(attacker != null)
				attacker.summoned.add(volcano);
		}
		if (proc.MINIVOLC.exists() && (capt.size() > 0 || proc.MINIVOLC.type.hitless)) {
			int dire = model.getDire();
			Proc.MINIVOLC volc = proc.MINIVOLC;
			int addp = volc.dis_0 == volc.dis_1 ? volc.dis_0 : volc.dis_0 + (int) (model.b.r.nextDouble() * (volc.dis_1 - volc.dis_0));
			float p0 = model.getPos() + dire * addp;
			float sta = p0 + (dire == 1 ? W_VOLC_PIERCE : W_VOLC_INNER);
			float end = p0 - (dire == 1 ? W_VOLC_INNER : W_VOLC_PIERCE);

			ContVolcano volcano = new ContVolcano(new AttackVolcano(attacker, this, sta, end, WT_MIVC), p0, layer, volc.time, false);
			volcano.v.raw *= proc.MINIVOLC.mult / 100.0;

			if(attacker != null)
				attacker.summoned.add(volcano);
		}
	}
}
