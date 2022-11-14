package common.battle.attack;

import common.battle.data.CustomEntity;
import common.battle.data.MaskAtk;
import common.battle.data.MaskEntity;
import common.battle.data.PCoin;
import common.battle.entity.*;
import common.util.Data;
import common.util.Data.Proc.SUMMON;
import common.util.pack.EffAnim.DefEff;
import common.util.unit.Level;

import java.util.Comparator;
import java.util.List;

public abstract class AtkModelEntity extends AtkModelAb {

	protected static final Proc sealed = Proc.blank();
	protected static final String[] par = { "KB", "STOP", "SLOW", "WEAK", "WARP", "CURSE", "SNIPER", "SEAL", "POISON", "BOSS",
			"POIATK", "ARMOR", "SPEED", "LETHARGY", "CRIT", "WAVE", "BREAK", "SATK", "VOLC", "MINIWAVE", "SHIELDBREAK", "WORKERLV", "CDSETTER"};
	/**
	 * @param e The entity
	 * @param d0 Level multiplication for EUnit, Magnification for EEnemy
	 * @return returns AtkModelEntity with specified magnification values
	 */
	public static AtkModelEntity getEnemyAtk(Entity e, double d0) {
		if (e instanceof EEnemy) {
			EEnemy ee = (EEnemy) e;
			return new AtkModelEnemy(ee, d0);
		}
		return null;
	}

	public static AtkModelEntity getUnitAtk(Entity e, double treasure, double level, PCoin pcoin, Level lv) {
		if(!(e instanceof EUnit))
			return null;

		return new AtkModelUnit(e, treasure, level, pcoin, lv);
	}

	public int atkType = 0;
	protected final double d0, d1;
	protected final MaskEntity data;
	public final Entity e;
	protected final int[][] act;

	protected AtkModelEntity(Entity ent, double d0, double d1) {
		super(ent.basis);
		e = ent;
		data = e.data;
		this.d0 = d0;
		this.d1 = d1;

		MaskAtk[][] matks = data.getAllAtks();
		act = new int[matks.length + 1][];
		for (int i = 0; i < matks.length; i++) {
			act[i] = new int[matks[i].length];
			for (int j = 0; j < act[i].length; j++)
				act[i][j] = data.getAtkModel(i, j).loopCount();
		}
		setExtraAtks(data.getSpAtks());
	}

	protected AtkModelEntity(Entity ent, double d0, double d1, PCoin pc, Level lv) {
		super(ent.basis);
		e = ent;
		data = e.data;
		this.d0 = d0;
		if (pc != null && lv != null && lv.getLvs().size() == pc.max.size())
			this.d1 = Math.round((pc.getAtkMultiplication(lv.getLvs()) * d1) * (1 + ent.basis.b.getInc(Data.C_ATK) * 0.01));
		else
			this.d1 = Math.round(d1 * (1 + ent.basis.b.getInc(Data.C_ATK) * 0.01));

		MaskAtk[][] matks = data.getAllAtks();
		act = new int[matks.length + 1][];
		for (int i = 0; i < matks.length; i++) {
			act[i] = new int[matks[i].length];
			for (int j = 0; j < act[i].length; j++)
				act[i][j] = data.getAtkModel(i, j).loopCount();
		}
		setExtraAtks(data.getSpAtks());
	}

	public void setExtraAtks(MaskAtk[] matks) {
		act[act.length - 1] = new int[matks.length];
		for(int i = 0; i < matks.length; i++)
			if(data.getSpAtks()[i] != null) {
				act[act.length - 1][i] = data.getSpAtks()[i].loopCount();
			}
	}

	@Override
	public int getAbi() {
		return e.getAbi();
	}

	/**
	 * get the attack, for display only
	 */
	public int getAtk() {
		int ans = 0, temp = 0, c = 1;
		MaskAtk[] atks = data.getAtks(atkType);
		for (int i = 0; i < atks.length; i++)
			if (atks[i].getPre() > 0) {
				ans += temp / c;
				temp = atks[i].getDire() > 0 ? getEffAtk(i) : 0;
				c = 1;
			} else {
				temp += atks[i].getDire() > 0 ? getEffAtk(i) : 0;
				c++;
			}
		ans += temp / c;
		return ans;
	}

	/**
	 * get damage from a specific attack, for AI only
	 */
	public int getAtk(int ind, int touch) {
		if (ind < data.getAtkCount(atkType) && getMAtk(ind).getDire() > 0 && (getMAtk(ind).getTarget() & touch) != 0) {
			return getEffAtk(ind);
		}
		return -1;
	}

	public int getEffAtk(MaskAtk matk) {
		int dmg = (int) (Math.round(matk.getAtk() * d0) * d1);
		if (e.status[P_WEAK][0] > 0)
			dmg = dmg * e.status[P_WEAK][1] / 100;
		if (e.status[P_STRONG][0] != 0)
			dmg += dmg * (e.status[P_STRONG][0]) / 100;
		dmg *= e.auras.getAtkAura();

		return dmg;
	}

	public int getEffAtk(int ind) {
		return getEffAtk(getMAtk(ind));
	}

	public int predictDamage(int ind) {
		int total = 0;
		MaskAtk[] atks = data.getAtks(ind);
		for (MaskAtk atk : atks) {
			int dmg = getEffAtk(atk);
			double[] ranges = inRange(atk);
			List<AbEntity> ents = e.basis.inRange(e.getTouch(), atk.getDire() * getDire(), ranges[0], ranges[1], false);
			for (AbEntity ent : ents)
				total += atk.getDire() == 1 ? Math.min(e.health, dmg * ent.calcDamageMult(dmg, e, atk)) : Math.max(e.maxH - e.health, -dmg * ent.calcDamageMult(dmg, e, atk));
		}
		return total;
	}

	/**
	 * generate attack entity
	 */
	public final AttackAb getAttack(int ind) {
		if (act[atkType][ind] == 0)
			return null;
		act[atkType][ind]--;
		Proc proc = Proc.blank();
		int atk = getAttack(ind, proc);
		double[] ints = inRange(ind);
		MaskAtk matk = getMAtk(ind);
		return new AttackSimple(e, this, atk, e.traits, getAbi(), proc, ints[0], ints[1], getMAtk(ind), e.layer, matk.isLD() || matk.isOmni());
	}

	/**
	 * generate attack entity for a special attack
	 */
	public final AttackAb getSpAttack(int ind) {
		int oldAtk = atkType;
		atkType = act.length - 1;
		AttackAb newAtk = getAttack(ind);
		atkType = oldAtk;
		return newAtk;
	}

	/**
	 * Generate death surge when this entity is killed and the surge procs
	 */
	public void getDeathSurge() {
		Proc p = Proc.blank();
		int atk = getAttack(0, p);
		AttackSimple as = new AttackSimple(e, this, atk, e.traits, getAbi(), p, 0, 0, e.data.getAtkModel(0, 0), 0, false);
		Proc.VOLC ds = e.getProc().DEATHSURGE;
		int addp = ds.dis_0 + (int) (b.r.nextDouble() * (ds.dis_1 - ds.dis_0));
		double p0 = getPos() + getDire() * addp;
		double sta = p0 + (getDire() == 1 ? W_VOLC_PIERCE : W_VOLC_INNER);
		double end = p0 - (getDire() == 1 ? W_VOLC_INNER : W_VOLC_PIERCE);

		new ContVolcano(new AttackVolcano(e, as, sta, end), p0, e.layer, ds.time);
	}

	@Override
	public int getDire() {
		return e.dire;
	}

	@Override
	public double getPos() {
		return e.pos;
	}

	/**
	 * get the attack box for maskAtk
	 */
	public double[] inRange(MaskAtk atk) {
		int dire = e.dire;
		double d0, d1;
		d0 = d1 = e.pos;
		if (!atk.isLD() && !atk.isOmni()) {
			d0 += data.getRange() * dire;
			d1 -= data.getWidth() * dire;
		} else {
			d0 += atk.getShortPoint() * dire;
			d1 += atk.getLongPoint() * dire;
		}
		return new double[] { d0, d1 };
	}

	/**
	 * get the attack box for nth attack
	 */
	public double[] inRange(int ind) {
		return inRange(getMAtk(ind));
	}

	@Override
	public void invokeLater(AttackAb atk, Entity e) {
		SUMMON proc = atk.getProc().SUMMON;
		if (proc.exists()) {
			SUMMON.TYPE conf = proc.type;
			if (conf.on_hit || (conf.on_kill && e.health <= 0)) {
				int rst = e.getProc().IMUSUMMON.mult;
				summon(proc, e, e, rst);
			}
		}
	}

	/**
	 * get the collide box bound
	 */
	public double[] touchRange() {
		int dire = e.dire;
		double d0, d1;
		d0 = d1 = e.pos;
		d0 += data.getRange() * dire;
		if (data.isLD() && !data.isOmni() && e.getProc().AI.type.calcblindspot)
			d1 += getBlindSpot() * dire;
		else
			d1 -= data.getWidth() * dire;
		return new double[] { d0, d1 };
	}

	protected void extraAtk(int ind) {
		if (getMAtk(ind).getMove() != 0)
			e.pos += getMAtk(ind).getMove() * e.dire;
		if (getMAtk(ind).getAltAbi() != 0)
			e.altAbi(getMAtk(ind).getAltAbi());

		if (getProc(ind).TIME.prob != 0 && (getProc(ind).TIME.prob == 100 || b.r.nextDouble() * 100 < getProc(ind).TIME.prob)) {
			if (getProc(ind).TIME.intensity > 0) {
				b.temp_s_stop = Math.max(b.temp_s_stop, getProc(ind).TIME.time);
				b.temp_inten = getProc(ind).TIME.intensity;
			} else {
				b.sn_temp_stop = Math.max(b.sn_temp_stop, getProc(ind).TIME.time);
				b.temp_n_inten = (float)Math.abs(getProc(ind).TIME.intensity) / b.sn_temp_stop;
			}
		}
		Proc.THEME t = getProc(ind).THEME;
		if (t.prob != 0 && (t.prob == 100 || b.r.nextDouble() * 100 < t.prob))
			b.changeTheme(t);
		Proc.PM w = getProc(ind).WORKERLV;
		if (w.prob != 0 && (w.prob == 100 || b.r.nextDouble() * 100 < w.prob))
			b.changeWorkerLv(w.mult);
		Proc.CDSETTER c = getProc(ind).CDSETTER;
		if (c.prob != 0 && (c.prob == 100 || b.r.nextDouble() * 100 < c.prob))
			b.changeUnitCooldown(c.amount, c.slot, c.type);
	}

	protected abstract int getAttack(int ind, Proc proc);

	public final MaskAtk getMAtk(int ind) {
		return data.getAtkModel(atkType, ind);
	}

	@Override
	protected int getLayer() {
		return e.layer;
	}

	public Proc getProc(MaskAtk matk) {
		if (e.status[P_SEAL][0] > 0)
			return sealed;
		return matk.getProc();
	}

	public Proc getProc(int ind) {
		return getProc(getMAtk(ind));
	}

	protected void setProc(int ind, Proc proc, int startOff) {

		for (int i = startOff; i < par.length; i++)
			if (getProc(ind).get(par[i]).perform(b.r))
				proc.get(par[i]).set(getProc(ind).get(par[i]));

		if (data instanceof CustomEntity)
			for (int b : BCShareable) proc.getArr(b).set(getProc(ind).getArr(b));
		if (getProc(ind).SUMMON.perform(b.r)) {
			SUMMON sprc = getProc(ind).SUMMON;
			SUMMON.TYPE conf = sprc.type;
			if (!conf.on_hit && !conf.on_kill)
				summon(sprc, e, data.getAtkModel(atkType, ind),0);
			else
				proc.SUMMON.set(sprc);
		}

		if (proc.CRIT.exists() && proc.CRIT.mult == 0)
			proc.CRIT.mult = 200;
		if (proc.KB.exists() && proc.KB.dis == 0)
			proc.KB.dis = KB_DIS[INT_KB];
		if (proc.KB.exists() && proc.KB.time == 0)
			proc.KB.time = KB_TIME[INT_KB];
		if (proc.BOSS.exists()) {
			b.lea.add(new EAnimCont(e.pos, e.layer, effas().A_SHOCKWAVE.getEAnim(DefEff.DEF)));
			b.lea.sort(Comparator.comparingInt(e -> e.layer));
		}
		if (proc.MINIWAVE.exists() && proc.MINIWAVE.multi == 0)
			proc.MINIWAVE.multi = 20;
	}

	public double getBlindSpot() {
		double blindspot = data.getWidth() * e.dire;
		if (data.isLD() && !data.isOmni()) {
			blindspot = Integer.MAX_VALUE;
			for (int i = 0; i < data.getAtkCount(0); i++)
				blindspot = Math.min(getMAtk(i).getShortPoint(), blindspot);

			if (blindspot >= data.getRange())
				blindspot = data.getWidth() * e.dire;
		}
		return blindspot;
	}

	public boolean isSupport() {
		//TODO
		for (int i = 0; i < data.getAtkCount(atkType); i++) {
			if (getEffAtk(i) <= 0)
				return true;
		}
		return false;
	}

	protected abstract void summon(SUMMON sprc, Entity ent, Object acs, int resist);

}
