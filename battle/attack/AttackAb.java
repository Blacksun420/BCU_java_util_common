package common.battle.attack;

import common.CommonStatic;
import common.battle.data.MaskAtk;
import common.battle.entity.AbEntity;
import common.battle.entity.Entity;
import common.pack.Identifier;
import common.pack.SortedPackSet;
import common.util.BattleObj;
import common.util.stage.Music;
import common.util.unit.Trait;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public abstract class AttackAb extends BattleObj {

	public final int abi;
	public int atk;
	public final SortedPackSet<Trait> trait;
	public final AtkModelAb model;
	public final AttackAb origin;
	public final MaskAtk matk;
	public final Entity attacker;
	public final int layer;
	public final boolean isLongAtk;
	public int duration;
	public boolean excludeRightEdge = false, isCounter = false;

	public int touch = TCH_N, dire, canon = -2, waveType = 0;

	protected final Proc proc;
	public final Set<Proc.REMOTESHIELD> r = new HashSet<>();
	protected final List<AbEntity> capt = new ArrayList<>();
	protected float sta, end;

	protected AttackAb(Entity attacker, AtkModelAb ent, int ATK, SortedPackSet<Trait> tr, int eab, Proc pro, float p0, float p1, MaskAtk matk, int layer, boolean isLongAtk, int time) {
		this.attacker = attacker;
		dire = ent.getDire();
		origin = this;
		model = ent;
		trait = matk != null && matk.getATKTraits().size() > 0 ? matk.getATKTraits() : tr;
		atk = ATK;
		proc = pro;
		abi = eab;
		sta = p0;
		end = p1;
		duration = time;
		this.matk = matk;
		this.layer = layer;
		this.isLongAtk = isLongAtk;
	}

	protected AttackAb(Entity attacker, AttackAb a, float STA, float END, boolean isLongAtk) {
		this.attacker = attacker;
		dire = a.dire;
		origin = a.origin;
		model = a.model;
		atk = a.atk;
		abi = a.abi;
		trait = a.trait;
		proc = a.proc;
		touch = a.touch;
		canon = a.canon;
		sta = STA;
		end = END;
		duration = 1;
		matk = a.matk;
		layer = a.layer;
		isCounter = a.isCounter;
		this.isLongAtk = isLongAtk;
	}

	/**
	 * capture the entities
	 */
	public abstract void capture();

	/**
	 * apply this attack to the entities captured
	 */
	public abstract void excuse();

	public Proc getProc() {
		return proc;
	}

	protected void process() {
		duration--;
		for (AbEntity ae : capt) {
			if (ae instanceof Entity) {
				Entity e = (Entity) ae;
				Proc imus = e.getProc();
				boolean blocked = false;
				if (proc.KB.dis > 0 && imus.IMUKB.block != 0) {
					if (imus.IMUKB.block > 0)
						blocked = true;
					if (imus.IMUKB.block == 100)
						proc.KB.clear();
					else
						proc.KB.dis *= (100 - imus.IMUKB.block) / 100.0;
				}
				if (proc.SLOW.time > 0 && imus.IMUSLOW.block != 0) {
					if (imus.IMUSLOW.block > 0)
						blocked = true;
					if (imus.IMUSLOW.block == 100)
						proc.SLOW.clear();
					else
						proc.SLOW.time *= (100 - imus.IMUSLOW.block) / 100.0;
				}
				if (proc.STOP.time > 0 && imus.IMUSTOP.block != 0) {
					if (imus.IMUSTOP.block > 0)
						blocked = true;
					if (imus.IMUSTOP.block == 100)
						proc.STOP.clear();
					else
						proc.STOP.time *= (100 - imus.IMUSTOP.block) / 100.0;
				}
				if (proc.WEAK.time > 0 && checkAIImmunity(proc.WEAK.mult - 100,imus.IMUWEAK.smartImu, imus.IMUWEAK.block > 0)) {
					if (imus.IMUWEAK.block > 0)
						blocked = true;
					if (imus.IMUWEAK.block == 100)
						proc.WEAK.clear();
					else
						proc.WEAK.time *= (100 - imus.IMUWEAK.block) / 100.0;
				}
				if (proc.LETHARGY.time > 0 && checkAIImmunity(proc.LETHARGY.mult,imus.IMULETHARGY.smartImu, imus.IMULETHARGY.block > 0)) {
					if (imus.IMULETHARGY.block > 0)
						blocked = true;
					if (imus.IMULETHARGY.block == 100)
						proc.LETHARGY.clear();
					else
						proc.LETHARGY.time *= (100 - imus.IMULETHARGY.block) / 100.0;
				}
				if (proc.WARP.time > 0 && imus.IMUWARP.block != 0) {
					if (imus.IMUWARP.block > 0)
						blocked = true;
					if (imus.IMUWARP.block == 100)
						proc.WARP.clear();
					else
						proc.WARP.time *= (100 - imus.IMUWARP.block) / 100.0;
				}
				if (proc.CURSE.time > 0 && imus.IMUCURSE.block != 0) {
					if (imus.IMUCURSE.block > 0)
						blocked = true;
					if (imus.IMUCURSE.block == 100)
						proc.CURSE.clear();
					else
						proc.CURSE.time *= (100 - imus.IMUCURSE.block) / 100.0;
				}
				if (proc.POIATK.mult != 0 && imus.IMUPOIATK.block != 0) {
					if (imus.IMUPOIATK.block > 0)
						blocked = true;
					if (imus.IMUPOIATK.block == 100)
						proc.POIATK.clear();
					else
						proc.POIATK.mult *= (100 - imus.IMUPOIATK.block) / 100.0;
				}
				if (proc.SUMMON.mult > 0 && imus.IMUSUMMON.block != 0) {
					if (imus.IMUSUMMON.block > 0)
						blocked = true;
					if (imus.IMUSUMMON.block == 100)
						proc.SUMMON.clear();
					else
						proc.SUMMON.mult *= (100 - imus.IMUSUMMON.block) / 100.0;
				}
				if (proc.CRIT.mult > 0 && imus.CRITI.block != 0) {
					if (imus.CRITI.block > 0)
						blocked = true;
					if (imus.CRITI.block == 100)
						proc.CRIT.clear();
					else
						proc.CRIT.mult *= (100 - imus.CRITI.block) / 100.0;
				}
				if (proc.POISON.damage != 0 && imus.IMUPOI.block != 0 && checkAIImmunity(proc.POISON.damage, imus.IMUPOI.smartImu, imus.IMUPOI.block < 0)) {
					if (imus.IMUPOI.block > 0)
						blocked = true;
					if (imus.IMUPOI.block == 100)
						proc.POISON.clear();
					else
						proc.POISON.damage *= (100 - imus.IMUPOI.block) / 100.0;
				}
				if (proc.SEAL.time > 0 && imus.IMUSEAL.block != 0) {
					if (imus.IMUSEAL.block > 0)
						blocked = true;
					if (imus.IMUSEAL.block == 100)
						proc.SEAL.clear();
					else
						proc.SEAL.time *= (100 - imus.IMUSEAL.block) / 100.0;
				}
				if (proc.RAGE.time > 0 && imus.IMURAGE.block != 0) {
					if (imus.IMURAGE.block > 0)
						blocked = true;
					if (imus.IMURAGE.block == 100)
						proc.RAGE.clear();
					else
						proc.RAGE.time *= (100 - imus.IMURAGE.block) / 100.0;
				}
				if (proc.HYPNO.time > 0 && imus.IMUHYPNO.block != 0) {
					if (imus.IMUHYPNO.block > 0)
						blocked = true;
					if (imus.IMUHYPNO.block == 100)
						proc.HYPNO.clear();
					else
						proc.HYPNO.time *= (100 - imus.IMUHYPNO.block) / 100.0;
				}
				if (proc.ARMOR.time > 0 && imus.IMUARMOR.block != 0 && checkAIImmunity(proc.ARMOR.mult, imus.IMUARMOR.smartImu, imus.IMUARMOR.block < 0)) {
					if (imus.IMUARMOR.block > 0)
						blocked = true;
					if (imus.IMUARMOR.block == 100)
						proc.ARMOR.clear();
					else
						proc.ARMOR.time *= (100 - imus.IMUARMOR.block) / 100.0;
				}
				if (proc.SPEED.time > 0 && imus.IMUSPEED.block != 0) {
					boolean b;
					if (proc.SPEED.type != 2)
						b = imus.IMUSPEED.block < 0;
					else
						b = (e.data.getSpeed() > proc.SPEED.speed && imus.IMUSPEED.block > 0) || (e.data.getSpeed() < proc.SPEED.speed && imus.IMUSPEED.block < 0);

					if (checkAIImmunity(proc.SPEED.speed, imus.IMUSPEED.smartImu, b)) {
						if (imus.IMUSPEED.block > 0)
							blocked = true;
						if (imus.IMUSPEED.block == 100)
							proc.ARMOR.clear();
						else
							proc.ARMOR.time *= (100 - imus.IMUSPEED.block) / 100.0;
					}
				}

				if (blocked)
					e.anim.getEff(STPWAVE);
			}
		}
	}

	/**
	 * Used to obtain whether controlled immunity will have effect or not
	 * @param val The effect of the proc
	 * @param side The side used by the smartImu
	 * @param invert Inverts the >,< signs depending on the proc
	 * @return idk
	 */
	private boolean checkAIImmunity(double val, int side, boolean invert) {
		if (side == 0)
			return true;
		if (invert) {
			return val * side < 0;
		} else {
			return val * side > 0;
		}
	}

	/**
	 * Plays the default hit sound. If this attack has a custom sound effect, it is played over the in-game sound effects
	 * @param isBase If attacked entity is base
	 * @param alt Plays SE 0 if true, SE 1 if false
	 */
	public void playSound(boolean isBase, boolean alt) {
		if (isBase)
			CommonStatic.setSE(SE_HIT_BASE);
		else {
			Identifier<Music> sfx0 = matk == null ? null : matk.getAudio(false);
			Identifier<Music> sfx1 = matk == null ? null : matk.getAudio(true);
			if (sfx0 == null && sfx1 == null)
				CommonStatic.setSE(alt ? SE_HIT_0 : SE_HIT_1);
			else if (alt || sfx1 == null)
				CommonStatic.setSE(sfx0);
			else
				CommonStatic.setSE(sfx1);
		}
	}

	public void notifyEntity(Consumer<Entity> notifier) {
		if (attacker != null)
			notifier.accept(attacker);
	}
}