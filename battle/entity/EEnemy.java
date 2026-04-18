package common.battle.entity;

import common.CommonStatic;
import common.battle.StageBasis;
import common.battle.attack.AtkModelUnit;
import common.battle.attack.AttackAb;
import common.battle.data.MaskAtk;
import common.battle.data.MaskEnemy;
import common.battle.data.Orb;
import common.pack.SortedPackSet;
import common.pack.UserProfile;
import common.util.anim.AnimU;
import common.util.anim.EAnimU;
import common.util.pack.EffAnim;
import common.util.stage.Revival;
import common.util.stage.SCDef;
import common.util.unit.Trait;

import java.util.Arrays;

public class EEnemy extends Entity {

	public final int mark;
	public final double mult, mula;
	public final int line;

	public Revival rev;
	public float door;

	public EEnemy(StageBasis b, MaskEnemy de, EAnimU ea, float magnif, float atkMagnif, int d0, int d1, int m, int l) {
		super(b, de, ea, atkMagnif, magnif);
		mult = magnif;
		mula = atkMagnif;
		mark = m;
		line = l;
		isBase = mark <= -1;
		spawnLayer = layer = d0 == d1 ? d0 : d0 + (int) (b.r.nextFloat() * (d1 - d0 + 1));
		traits = new SortedPackSet<>(de.getTraits(false));

		skipSpawnBurrow = mark >= 1;
	}

	@Override
	public void kill(boolean glass) {
		super.kill(glass);

		if (basis.st.drop && !glass && basis.maxBankLimit() <= 0) {
			double mul = basis.b.t().getDropMulti(basis.elu.getInc(C_MEAR)) * (1 + (status.money / 100));
			basis.money = (int) (basis.money + mul * ((MaskEnemy) data).getDrop());
		}
		if (rev != null) {
			rev.triggerRevival(basis, basis.est.mul, layer, group, pos, line);
			if (anim.deathSurge == 0 && rev.soul != null)
				anim.dead = rev.soul.get().getEAnim(AnimU.SOUL[0]).len();
		}
		if (mark >= 1 && basis.st.bossGuard) {
			basis.baseBarrier--;
			if (basis.baseBarrier == 0) {
				if (basis.ebase instanceof ECastle) {
					((ECastle) basis.ebase).guard = effas().A_E_GUARD.getEAnim(EffAnim.GuardEff.BREAK);
					CommonStatic.setSE(SE_BARRIER_ABI);
				} else
					((EEnemy)basis.ebase).anim.getEff(A_GUARD_BRK);
			}
		}
		if (basis.st.trail && !basis.isDojoOvertime() && basis.isActive() && !glass) {
			SCDef.Line d = basis.st.data.getSimple(line);
			int time = basis.st.timeLimit * 1800;
			int score = (int) (((MaskEnemy) data).getDrop() / 100f + (d.score * (2f * time - basis.time)) / time);
			basis.score += score;
		}
	}

	@Override
	public float calcDamageMult(int dmg, Entity e, MaskAtk matk) {
		float ans = super.calcDamageMult(dmg, e, matk);
		if (ans == 0)
			return 0;
		if (e instanceof EUnit) {
			if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_WITCH)) && (e.getAbi() & AB_WKILL) > 0)
				ans *= basis.b.t().getWKAtk(basis.elu.getInc(C_WKILL, (EUnit)e));
			if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_EVA)) && (e.getAbi() & AB_EKILL) > 0)
				ans *= basis.b.t().getEKAtk(basis.elu.getInc(C_EKILL, (EUnit)e));
			if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_BARON)) && (e.getAbi() & AB_BAKILL) > 0)
				ans *= 1.6f;
			if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_BEAST)) && matk.getProc().BSTHUNT.active)
				ans *= 2.5f;
			if (traits.contains(BCTraits.get(TRAIT_SAGE)) && (e.getAbi() & AB_SKILL) > 0)
				ans *= SUPER_SAGE_HUNTER_ATTACK;
			if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_VILLAIN))) {
				if ((e.getAbi() & AB_VKILL) > 0)
					ans *= VILLAIN_KILLER_ATTACK;
				ans *= (float) (1 + basis.elu.getInc(C_VKILL, (EUnit) e) / 1000);
			}
		}
		return ans;
	}

	@Override
	protected void sumDamage(int atk, boolean raw) {
		if (CommonStatic.getConfig().rawDamage == raw)
			basis.dmgStatistics.get(data.getPack())[1] += atk;
	}

	@Override
	public boolean damaged(AttackAb atk) {
		if (isBase && dire == 1 && basis.baseBarrier > 0) {
			anim.getEff(A_GUARD);
			return false;
		}
		return super.damaged(atk);
	}

	@Override
	protected int getDamage(AttackAb atk, int ans) {
		ans = super.getDamage(atk, ans);
		if (atk.model instanceof AtkModelUnit) {
			SortedPackSet<Trait> sharedTraits = traits.inCommon(atk.trait);

			if (!sharedTraits.isEmpty()) {
				if (atk.attacker.status.curse == 0 && atk.attacker.getProc().DMGINC.mult != 0) {
					ans *= EUnit.OrbHandler.getOrb(atk.attacker.getProc().DMGINC.mult, atk, sharedTraits, basis.b.t());
					byte type = atk.attacker.getProc().DMGINC.getType(false);
					if (type >= 0)
						basis.scoreActivated(type == 0 ? SCORE_GOOD : type == 1 ? SCORE_MASSIVE : SCORE_MASSIVES, 1, atk.trait.size());
				}
				if (status.curse == 0 && getProc().DEFINC.mult != 0) {
					ans /= getProc().DEFINC.mult / 100.0;
					byte type = atk.attacker.getProc().DMGINC.getType(true);
					if (type >= 0)
						basis.scoreActivated(type == 0 ? SCORE_GOOD : type == 1 ? SCORE_RESIST : SCORE_RESISTS, 1, atk.trait.size());
				}
			}
			if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_WITCH)) && (atk.abi & AB_WKILL) > 0)
				ans *= basis.b.t().getWKAtk(basis.elu.getInc(C_WKILL, (EUnit)atk.attacker));
			if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_EVA)) && (atk.abi & AB_EKILL) > 0)
				ans *= basis.b.t().getEKAtk(basis.elu.getInc(C_EKILL, (EUnit)atk.attacker));
			if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_BARON))) {
				if ((atk.abi & AB_BAKILL) > 0)
					ans = (int)(ans * 1.6);
				if(((EUnit)atk.attacker).level.getOrbs() != null) {
					int[][] levelOrbs = ((EUnit)atk.attacker).level.getOrbs();
					for (int[] orb : levelOrbs)
						if (orb.length == ORB_TOT && orb[ORB_TYPE] == ORB_BAKILL)
							ans = (int)(ans * Orb.get(ORB_BAKILL,(byte)orb[ORB_GRADE])[0] / 100.0);
				}
			}
			if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_BEAST)) && atk.getProc().BSTHUNT.active)
				ans *= 2.5;
			if (traits.contains(BCTraits.get(TRAIT_SAGE)) && (atk.abi & AB_SKILL) > 0)
				ans = (int) (ans * SUPER_SAGE_HUNTER_ATTACK);
			if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_VILLAIN))) {
				if ((atk.abi & AB_VKILL) > 0)
					ans = (int) (ans * VILLAIN_KILLER_ATTACK);
				ans = (int) (ans * (1 + basis.elu.getInc(C_VKILL, (EUnit) atk.attacker) / 1000.0));
			}
		}
		if (atk.canon == 16)
			if ((touchable() & TCH_UG) > 0)
				ans = (int) (maxH * basis.b.t().getCannonMagnification(5, BASE_HOLY_ATK_UNDERGROUND));
			else
				ans = (int) (maxH * basis.b.t().getCannonMagnification(5, BASE_HOLY_ATK_SURFACE));
		ans = critCalc((getAbi() & AB_METALIC) != 0 || traits.contains(UserProfile.getBCData().traits.get(TRAIT_METAL)), ans, atk);

		// Perform Orb
		ans += EUnit.OrbHandler.getOrbAtk(atk, this);

		return ans;
	}

	@Override
	protected float getLim() {
		float ans;
		float minPos = ((MaskEnemy) data).getLimit();

		if (mark >= 1)
			ans = pos - (minPos + basis.boss_spawn); // guessed value compared to BC
		else
			ans = pos - minPos;
		return Math.max(0, ans);
	}

	@Override
	public float getResistValue(AttackAb atk, boolean SageRes, double procResist) {
		float ans = (float) ((100f - procResist) / 100f);

		if (SageRes && (atk.abi & AB_SKILL) == 0 && traits.contains(BCTraits.get(TRAIT_SAGE)))
			ans *= SUPER_SAGE_RESIST;
		return ans;
	}

	@Override
	public boolean processProcs(AttackAb atk) {
		boolean doCheck = super.processProcs(atk);
		if (!doCheck)
			return false;
		Proc atkProc = atk.getProc();

		if (atkProc.DELAY.exists() && line != -1 && basis.est.num[line] > 0 && basis.est.rem[line] > 0) {
			Proc.DELAY d = atkProc.DELAY;
			Proc.IMUAD imu = getProc().IMUDELAY;
			float res;
			if (imu.checkImu(d.strength))
				res = getResistValue(atk, true, imu.mult);
			else
				res = 0;
			if (res < 100) {
				int strength = (int) (d.strength * res);
				if (strength != 0) {
					if (d.type.ordinal() == 3)
						basis.est.rem[line] = strength;
					else
						status.delay[d.type.ordinal()] += strength;
					basis.lea.add(new EAnimCont(pos, layer, effas().A_E_DELAY.getEAnim(EffAnim.DefEff.DEF), -50f));
				}
				basis.scoreActivated(P_DELAY, 1, atk.trait.size());
			} else {
				anim.getEff(INV);
			}
		}

		return true;
	}

	@Override
	public void postUpdate() {
		if (Arrays.stream(status.delay).anyMatch(v -> v != 0)) {
			for (int i = 0; i < 3; i++) {
				basis.est.lineDelay[line][i] = status.delay[i];
				status.delay[i] = 0;
			}
		}

		if (skipSpawnBurrow && notAttacking())
			skipSpawnBurrow = status.burs[0] == 0;
		super.postUpdate();
	}

	@Override
	public double buff(int lv) {
		return lv * mult * mula;
	}
}
