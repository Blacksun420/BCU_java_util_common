package common.battle.attack;

import common.battle.data.CustomEntity;
import common.battle.data.MaskAtk;
import common.battle.data.MaskEntity;
import common.battle.data.PCoin;
import common.battle.entity.*;
import common.pack.Identifier;
import common.util.Data;
import common.util.Data.Proc.SUMMON;
import common.util.pack.EffAnim.DefEff;
import common.util.unit.*;
import org.jcodec.common.tools.MathUtil;

import java.util.List;

public abstract class AtkModelEntity extends AtkModelAb {

	public static final String[] par = { "SUMMON", "KB", "STOP", "SLOW", "WEAK", "WARP", "CURSE", "SNIPER", "SEAL", "POISON", "BOSS", "RAGE", "HYPNO", "POIATK",
			"ARMOR", "SPEED", "LETHARGY", "ATKBASE", "CRIT", "WAVE", "BREAK", "SATK", "VOLC", "MINIVOLC", "MINIWAVE", "MOVEWAVE", "SHIELDBREAK", "WORKERLV", "CDSETTER"};
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
		MaskAtk[][] satks = data.getSpAtks(false);
		act = new int[matks.length + satks.length][];
		setAtks(matks, satks);
	}

	protected AtkModelEntity(Entity ent, double d0, double d1, PCoin pc, Level lv) {
		super(ent.basis);
		e = ent;
		data = e.data;
		if (pc != null && lv != null && lv.getTalents().length == pc.max.length)
			this.d0 = d0 * pc.getStatMultiplication(PC2_ATK, lv.getTalents());
		else
			this.d0 = d0;
		this.d1 = d1 * (1 + ent.basis.b.getInc(Data.C_ATK) * 0.01);

		MaskAtk[][] matks = data.getAllAtks();
		MaskAtk[][] satks = data.getSpAtks(false);
		act = new int[matks.length + satks.length][];
		setAtks(matks, satks);
	}

	protected void setAtks(MaskAtk[][] matks, MaskAtk[][] satks) {
		for (int i = 0; i < matks.length; i++) {
			act[i] = new int[matks[i].length];
			for (int j = 0; j < act[i].length; j++)
				act[i][j] = data.getAtkModel(i, j).loopCount();
		}
		for(int i = 0; i < satks.length; i++) {
			int ind = act.length - satks.length + i;
			act[ind] = new int[satks[i].length];
			for (int j = 0; j < act[ind].length; j++)
				act[ind][j] = satks[i][j].loopCount();
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
			if (atks[i].getPre() > 0 || atks[i].getName().toLowerCase().startsWith("combo")) {
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

	public int getEffAtk(int ind) {
		return getEffAtk(getMAtk(ind));
	}
	public int getEffAtk(MaskAtk matk) {
		int dmg = (int) (Math.round(matk.getAtk() * d0) * d1);
		if (e.status.weak[0] > 0)
			dmg = dmg * e.status.weak[1] / 100;
		if (e.status.strengthen != 0)
			dmg += dmg * e.status.strengthen / 100;
		dmg *= e.auras.getAtkAura();

		return dmg;
	}

	public int predictDamage(int ind) {
		int total = 0;
		MaskAtk[] atks = data.getAtks(ind);
		for (MaskAtk atk : atks) {
			int dmg = getEffAtk(atk) * atk.getDire();
			double[] ranges = inRange(atk);
			List<AbEntity> ents = e.basis.inRange(atk.getTarget(), atk.getDire() * getDire(), ranges[0], ranges[1], false);
			for (AbEntity ent : ents)
				total += Math.min(e.health * atk.getDire(), dmg * ent.calcDamageMult(dmg, e, atk));
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
		MaskAtk matk = getMAtk(ind);
		int atk = getAttack(matk, proc);
		double[] ints = inRange(matk);
		return new AttackSimple(e, this, atk, e.traits, getAbi(), proc, ints[0], ints[1], matk, e.layer, matk.isLD() || matk.isOmni());
	}

	/**
	 * generate attack entity for a special attack
	 */
	public final AttackAb getSpAttack(int atkind, int ind) {
		int actind = data.getAtkTypeCount() + atkind;
		if (act[actind][ind] == 0)
			return null;
		act[actind][ind]--;
		Proc proc = Proc.blank();
		MaskAtk matk = data.getSpAtks(atkind)[ind];
		int atk = getAttack(matk, proc);
		double[] ints = inRange(matk);
		return new AttackSimple(e, this, atk, e.traits, getAbi(), proc, ints[0], ints[1], matk, e.layer, matk.isLD() || matk.isOmni());
	}

	/**
	 * Generate death surge when this entity is killed and the surge procs
	 */
	public void getDeathSurge() {
		Proc p = Proc.blank();
		int atk = getAttack(data.getAtkModel(0, 0), p);
		AttackSimple as = new AttackSimple(e, this, atk, e.traits, getAbi(), p, 0, 0, e.data.getAtkModel(0, 0), 0, false);
		Proc.VOLC ds = e.getProc().DEATHSURGE;
		int addp = ds.dis_0 == ds.dis_1 ? ds.dis_0 : ds.dis_0 + (int) (b.r.nextDouble() * (ds.dis_1 - ds.dis_0));
		double p0 = getPos() + getDire() * addp;
		double sta = p0 + (getDire() == 1 ? W_VOLC_PIERCE : W_VOLC_INNER);
		double end = p0 - (getDire() == 1 ? W_VOLC_INNER : W_VOLC_PIERCE);

		new ContVolcano(new AttackVolcano(e, as, sta, end, WT_VOLC), p0, e.layer, ds.time);
	}

	@Override
	public int getDire() {
		return e.getDire();
	}

	@Override
	public double getPos() {
		return e.pos;
	}

	/**
	 * get the attack box for maskAtk
	 */
	public double[] inRange(MaskAtk atk) {
		int dire = e.getDire();
		double d0, d1;
		d0 = d1 = dire == e.dire ? e.pos : e.pos + (data.getWidth() * dire);
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
				summon(proc, e, atk, rst);
			}
		}
	}

	/**
	 * get the collide box bound
	 */
	public double[] touchRange() {
		int dire = e.getDire();
		double d0, d1;
		d0 = d1 = dire == e.dire ? e.pos : e.pos + (data.getWidth() * dire);
		d0 += data.getRange() * dire;
		if (data.isLD() && e.getProc().AI.type.calcblindspot)
			d1 += getBlindSpot() * dire;
		else
			d1 -= data.getWidth() * dire;
		return new double[] { d0, d1 };
	}

	protected void extraAtk(MaskAtk matk) {
		if (matk.getMove() != 0)
			e.pos += matk.getMove() * e.getDire();
		if (matk.getAltAbi() != 0)
			e.altAbi(matk.getAltAbi());

		if (getProc(matk).TIME.prob != 0 && (getProc(matk).TIME.prob == 100 || b.r.nextDouble() * 100 < getProc(matk).TIME.prob)) {
			if (getProc(matk).TIME.intensity > 0) {
				b.temp_s_stop = Math.max(b.temp_s_stop, getProc(matk).TIME.time);
				b.temp_inten = getProc(matk).TIME.intensity;
			} else {
				b.sn_temp_stop = Math.max(b.sn_temp_stop, getProc(matk).TIME.time);
				b.temp_n_inten = (float)Math.abs(getProc(matk).TIME.intensity) / b.sn_temp_stop;
			}
		}
		Proc.THEME t = getProc(matk).THEME;
		if (t.prob != 0 && (t.prob == 100 || b.r.nextDouble() * 100 < t.prob))
			b.changeTheme(t);
		Proc.PM w = getProc(matk).WORKERLV;
		if (w.prob != 0 && (w.prob == 100 || b.r.nextDouble() * 100 < w.prob))
			b.changeWorkerLv(w.mult);
		Proc.CDSETTER c = getProc(matk).CDSETTER;
		if (c.prob != 0 && (c.prob == 100 || b.r.nextDouble() * 100 < c.prob))
			b.changeUnitCooldown(c.amount, c.slot, c.type);
	}

	protected abstract int getAttack(MaskAtk ind, Proc proc);

	public final MaskAtk getMAtk(int ind) {
		return data.getAtkModel(atkType, ind);
	}

	@Override
	protected int getLayer() {
		return e.layer;
	}

	public Proc getProc(MaskAtk matk) {
		if (e.status.seal > 0)
			return empty;
		return matk.getProc();
	}

	public Proc getProc(int ind) {
		return getProc(getMAtk(ind));
	}

	protected void setProc(MaskAtk matk, Proc proc, int startOff) {

		for (int i = startOff; i < par.length; i++)
			if (getProc(matk).get(par[i]).perform(b.r))
				proc.get(par[i]).set(getProc(matk).get(par[i]));

		if (data instanceof CustomEntity)
			for (int b : BCShareable) proc.getArr(b).set(getProc(matk).getArr(b));
		if (getProc(matk).SUMMON.perform(b.r)) {
			SUMMON sprc = getProc(matk).SUMMON;
			SUMMON.TYPE conf = sprc.type;
			if (!conf.on_hit && !conf.on_kill)
				summon(sprc, e, matk, 0);
			else
				proc.SUMMON.set(sprc);
		}

		if (proc.CRIT.exists() && proc.CRIT.mult == 0)
			proc.CRIT.mult = 200;
		if (proc.KB.exists() && proc.KB.dis == 0)
			proc.KB.dis = KB_DIS[INT_KB];
		if (proc.KB.exists() && proc.KB.time == 0)
			proc.KB.time = KB_TIME[INT_KB];
		if (proc.BOSS.exists())
			b.lea.add(new EAnimCont(e.pos, e.layer, effas().A_SHOCKWAVE.getEAnim(DefEff.DEF)));
		if (proc.MINIWAVE.exists() && proc.MINIWAVE.multi == 0)
			proc.MINIWAVE.multi = 20;
		if (proc.MINIVOLC.exists() && proc.MINIVOLC.mult == 0)
			proc.MINIVOLC.mult = 20;
	}

	public double getBlindSpot() {
		double blindspot = data.getWidth() * e.getDire();
		if (data.isLD()) {
			blindspot = Integer.MAX_VALUE;
			for (int i = 0; i < data.getAtkCount(0); i++)
				blindspot = Math.min(getMAtk(i).getShortPoint(), blindspot);

			if (blindspot >= data.getRange())
				blindspot = data.getWidth() * e.getDire();
		}
		return blindspot;
	}

	protected void summon(SUMMON proc, Entity ent, Object acs, int resist) {
		if (resist < 100) {
			SUMMON.TYPE conf = proc.type;
			if (conf.same_health && ent.health <= 0)
				return;
			int time = proc.time;
			int minlayer = proc.min_layer, maxlayer = proc.max_layer;
			if (proc.min_layer == proc.max_layer && proc.min_layer == -1)
				minlayer = maxlayer = e.layer;

			if ((proc.id == null && e instanceof EUnit) || (proc.id != null && AbUnit.class.isAssignableFrom(proc.id.cls))) {
				AbUnit u = Identifier.getOr(proc.id, AbUnit.class);
				if (b.entityCount(-1) < b.max_num - u.getForms()[proc.form - 1].du.getWill() || conf.ignore_limit) {
					int lvl = proc.mult;
					if (!conf.fix_buff)
						lvl = (int) e.buff(lvl);
					lvl *= (100.0 - resist) / 100;
					lvl = MathUtil.clip(lvl, 1, u.getCap());

					for (int i = 0; i < proc.amount; i++) {
						int dis = proc.dis == proc.max_dis ? proc.dis : (int) (proc.dis + b.r.nextDouble() * (proc.max_dis - proc.dis + 1));
						double up = ent.pos + getDire() * dis;
						IForm ef = IForm.newIns(u instanceof Unit ? u.getForms()[Math.max(proc.form - 1, 0)] : (AbForm)u, lvl);
						EUnit eu = ef.invokeEntity(b, lvl, minlayer, maxlayer);
						if (conf.same_health)
							eu.health = e.health;

						eu.added(-1, (int) up);
						b.tempe.add(new EntCont(eu, time));
						eu.setSummon(conf.anim_type, conf.bond_hp ? e : null);

						if ((proc.type.pass_proc & 1) > 0)
							eu.status.pass(e.status);
						if (e != ent && (proc.type.pass_proc & 2) > 0)
							eu.status.pass(ent.status);
					}
				}
			} else {
				AbEnemy ene = Identifier.getOr(proc.id, AbEnemy.class);
				int allow = b.st.data.allow(b, ene);
				if (allow >= 0 || conf.ignore_limit) {
					double mula = proc.mult * 0.01;
					double mult = proc.mult * 0.01;
					if (!conf.fix_buff) {
						if (e instanceof EUnit) {
							mula *= 1 + ((((EUnit) e).lvl - 1) * 0.2);
							mult *= 1 + ((((EUnit) e).lvl - 1) * 0.2);
						} else {
							mult *= ((EEnemy) e).mult;
							mula *= ((EEnemy) e).mula;
						}
					}

					mula *= (100.0 - resist) / 100;
					mult *= (100.0 - resist) / 100;
					for (int i = 0; i < proc.amount; i++) {
						int dis = proc.dis == proc.max_dis ? proc.dis : (int) (proc.dis + b.r.nextDouble() * (proc.max_dis - proc.dis + 1));
						double up = ent.pos + getDire() * dis;
						EEnemy ee = ene.getEntity(b, acs, mult, mula, minlayer, maxlayer, 0);

						ee.group = allow;
						if (up < ee.data.getWidth())
							up = ee.data.getWidth();
						if (up > b.st.len - 800)
							up = b.st.len - 800;

						ee.added(1, (int) up);
						b.tempe.add(new EntCont(ee, time));
						if (conf.same_health)
							ee.health = e.health;
						ee.setSummon(conf.anim_type, conf.bond_hp ? e : null);

						if ((proc.type.pass_proc & 1) > 0)
							ee.status.pass(e.status);
						if (e != ent && (proc.type.pass_proc & 2) > 0)
							ee.status.pass(ent.status);

						if (!b.enemyStatistics.containsKey((Enemy)ee.data.getPack()))
							b.enemyStatistics.put((Enemy)ee.data.getPack(), new long[]{0, 0, 1});
						else
							b.enemyStatistics.get((Enemy)ee.data.getPack())[2]++;
					}
				}
			}
		} else
			ent.anim.getEff(INV);
	}

}
