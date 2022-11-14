package common.battle.entity;

import common.battle.StageBasis;
import common.battle.attack.AtkModelUnit;
import common.battle.attack.AttackAb;
import common.battle.attack.AttackWave;
import common.battle.data.MaskAtk;
import common.battle.data.MaskEnemy;
import common.battle.data.MaskUnit;
import common.pack.UserProfile;
import common.util.anim.EAnimU;
import common.util.unit.Trait;

import java.util.ArrayList;

public class EEnemy extends Entity {

	public final int mark;
	public final double mult, mula;

	public float door;
	public byte hit;

	public EEnemy(StageBasis b, MaskEnemy de, EAnimU ea, double magnif, double atkMagnif, int d0, int d1, int m) {
		super(b, de, ea, atkMagnif, magnif);
		mult = magnif;
		mula = atkMagnif;
		mark = m;
		isBase = mark <= -1;
		layer = d0 == d1 ? d0 : d0 + (int) (b.r.nextDouble() * (d1 - d0 + 1));
		traits = de.getTraits();

		canBurrow = mark < 1;
	}

	@Override
	public void kill(boolean atk) {
		super.kill(atk);
		if (!basis.st.trail && !atk) {
			double mul = basis.b.t().getDropMulti() * (1 + (status[P_BOUNTY][0] / 100.0));
			basis.money += mul * ((MaskEnemy) data).getDrop();
		}
	}

	@Override
	public float calcDamageMult(int dmg, Entity e, MaskAtk matk) {
		float ans = super.calcDamageMult(dmg, e, matk);
		if (ans == 0)
			return 0;
		if (status[P_BARRIER][0] != 0) {
			if (matk.getProc().BREAK.prob > 0) {
				ans *= matk.getProc().BREAK.prob / 100f;
				if (dmg >= status[P_BARRIER][0]) {
					return ans * status[P_BARRIER][0] / dmg;
				}
			} else if (dmg >= status[P_BARRIER][0]) {
				return 1f * status[P_BARRIER][0] / dmg;
			} else {
				return 0;
			}
		}
		if (e instanceof EUnit) {
			ArrayList<Trait> sharedTraits = new ArrayList<>(matk.getATKTraits());
			sharedTraits.retainAll(traits);
			boolean isAntiTraited = targetTraited(matk.getATKTraits());
			for (Trait t : traits) {
				if (t.BCTrait || sharedTraits.contains(t))
					continue;
				if ((t.targetType && isAntiTraited) || t.others.contains(((MaskUnit)e.data).getPack()))
					sharedTraits.add(t);
			}

			if (!sharedTraits.isEmpty()) {
				if (e.status[P_CURSE][0] == 0) {
					if ((e.getAbi() & AB_GOOD) != 0)
						ans *= 1.5;
					if ((e.getAbi() & AB_MASSIVE) != 0)
						ans *= 3;
					if ((e.getAbi() & AB_MASSIVES) != 0)
						ans *= 5;
				}
				if (status[P_CURSE][0] == 0) {
					if ((getAbi() & AB_GOOD) > 0)
						ans /= 2;
					if ((getAbi() & AB_RESIST) > 0)
						ans /= 4;
					if ((getAbi() & AB_RESISTS) > 0)
						ans /= 6;
				}
			}
			if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_WITCH)) && (e.getAbi() & AB_WKILL) > 0)
				ans *= basis.b.t().getWKAtk();
			if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_EVA)) && (e.getAbi() & AB_EKILL) > 0)
				ans *= basis.b.t().getEKAtk();
			if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_BARON)) && (e.getAbi() & AB_BAKILL) > 0)
				ans *= 1.6;
			if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_BEAST)) && matk.getProc().BSTHUNT.type.active)
				ans *= 2.5;
		}
		return ans;
	}

	@Override
	protected int getDamage(AttackAb atk, int ans) {
		if (atk instanceof AttackWave && atk.waveType == WT_MINI) {
			ans = (int) ((double) ans * atk.getProc().MINIWAVE.multi / 100.0);
		}
		if (atk.model instanceof AtkModelUnit) {
			ArrayList<Trait> sharedTraits = new ArrayList<>(atk.trait);
			sharedTraits.retainAll(traits);
			boolean isAntiTraited = targetTraited(atk.trait);
			for (Trait t : traits) {
				if (t.BCTrait || sharedTraits.contains(t))
					continue;
				if ((t.targetType && isAntiTraited) || t.others.contains(((MaskUnit)atk.attacker.data).getPack()))
					sharedTraits.add(t);
			}

			if (!sharedTraits.isEmpty()) {
				if (atk.attacker.status[P_CURSE][0] == 0) {
					if ((atk.abi & AB_GOOD) != 0)
						ans *= EUnit.OrbHandler.getOrbGood(atk, sharedTraits, basis.b.t());
					if ((atk.abi & AB_MASSIVE) != 0)
						ans *= EUnit.OrbHandler.getOrbMassive(atk, sharedTraits, basis.b.t());
					if ((atk.abi & AB_MASSIVES) != 0)
						ans *= basis.b.t().getMASSIVESATK(sharedTraits);
				}
				if (status[P_CURSE][0] == 0) {
					if ((getAbi() & AB_GOOD) > 0)
						ans /= 2;
					if ((getAbi() & AB_RESIST) > 0)
						ans /= 4;
					if ((getAbi() & AB_RESISTS) > 0)
						ans /= 6;
				}
			}
			if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_WITCH)) && (atk.abi & AB_WKILL) > 0)
				ans *= basis.b.t().getWKAtk();
			if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_EVA)) && (atk.abi & AB_EKILL) > 0)
				ans *= basis.b.t().getEKAtk();
			if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_BARON)) && (atk.abi & AB_BAKILL) > 0)
				ans *= 1.6;
			if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_BEAST)) && atk.getProc().BSTHUNT.type.active)
				ans *= 2.5;
		}
		if (isBase)
			ans *= 1 + atk.getProc().ATKBASE.mult / 100.0;
		if (atk.canon == 16)
			if ((touchable() & TCH_UG) > 0)
				ans = (int) (maxH * basis.b.t().getCannonMagnification(5, BASE_HOLY_ATK_UNDERGROUND));
			else
				ans = (int) (maxH * basis.b.t().getCannonMagnification(5, BASE_HOLY_ATK_SURFACE));
		ans = critCalc(data.getTraits().contains(UserProfile.getBCData().traits.get(TRAIT_METAL)), ans, atk);

		// Perform Orb
		ans += EUnit.OrbHandler.getOrbAtk(atk, this);

		return ans;
	}

	@Override
	protected double getLim() {
		double ans;
		double minPos = ((MaskEnemy) data).getLimit();

		if (mark >= 1)
			ans = pos - (minPos + basis.boss_spawn); // guessed value compared to BC
		else
			ans = pos - minPos;
		return Math.max(0, ans);
	}

	@Override
	protected int traitType() {
		return 1;
	}

	@Override
	public void damaged(AttackAb atk) {
		hit = 2;
		super.damaged(atk);
	}

	@Override
	public void update() {
		if(hit > 0) {
			hit--;
		}

		super.update();
	}

	@Override
	public void postUpdate() {
		super.postUpdate();
	}

	@Override
	protected void onLastBreathe() {
	}
}
