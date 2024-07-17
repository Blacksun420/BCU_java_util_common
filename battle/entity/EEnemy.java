package common.battle.entity;

import common.battle.StageBasis;
import common.battle.attack.AtkModelUnit;
import common.battle.attack.AttackAb;
import common.battle.attack.AttackVolcano;
import common.battle.attack.AttackWave;
import common.battle.data.MaskEnemy;
import common.battle.data.MaskUnit;
import common.pack.UserProfile;
import common.util.Data;
import common.util.anim.EAnimU;
import common.util.unit.Trait;

import java.util.ArrayList;

public class EEnemy extends Entity {

	public final int mark;
	public final double mult, mula;

	public byte hit;

	public EEnemy(StageBasis b, MaskEnemy de, EAnimU ea, float magnif, float atkMagnif, int d0, int d1, int m) {
		super(b, de, ea, atkMagnif, magnif);
		mult = magnif;
		mula = atkMagnif;
		mark = m;
		isBase = mark <= -1;
		layer = d0 == d1 ? d0 : d0 + (int) (b.r.nextFloat() * (d1 - d0 + 1));
		traits = de.getTraits();

		canBurrow = mark < 1;
	}

	@Override
	public int getAtk() {
		int atk = aam.getAtk();
		if (status[P_STRONG][0] != 0)
			atk += atk * status[P_STRONG][0] / 100;
		if (status[P_WEAK][0] > 0)
			atk = atk * status[P_WEAK][1] / 100;
		return atk;
	}

	@Override
	public void kill(KillMode atk) {
		super.kill(atk);

		if (!basis.st.trail && atk == KillMode.NORMAL && basis.maxBankLimit() <= 0) {
			float mul = basis.b.t().getDropMulti(basis.isBanned(Data.C_MEAR)) * (1 + (status[P_BOUNTY][0] / 100f));
			basis.money = (int) (basis.money + mul * ((MaskEnemy) data).getDrop());
		}
	}

	@Override
	protected int getDamage(AttackAb atk, int ans) {
		if (atk instanceof AttackWave && atk.waveType == WT_MINI) {
			ans = (int) ((float) ans * atk.getProc().MINIWAVE.multi / 100.0);
		}

		if (atk instanceof AttackVolcano && atk.waveType == WT_MIVC) {
			ans = (int) ((float) ans * atk.getProc().MINIVOLC.mult / 100.0);
		}

		if (atk.model instanceof AtkModelUnit && ((AtkModelUnit) atk.model).e.status[P_CURSE][0] == 0) {
			ArrayList<Trait> sharedTraits = new ArrayList<>(atk.trait);

			sharedTraits.retainAll(traits);

			boolean isAntiTraited = targetTraited(atk.trait);

			for (Trait t : traits) {
				if (t.BCTrait || sharedTraits.contains(t))
					continue;

				if ((t.targetType && isAntiTraited) || t.others.contains(((MaskUnit)atk.attacker.data).getPack()))
					sharedTraits.add(t);
			}

			if (!sharedTraits.isEmpty() && (atk.abi & AB_GOOD) != 0)
				ans = (int) (ans * EUnit.OrbHandler.getOrbGood(atk, sharedTraits, basis.b.t()));

			if (!sharedTraits.isEmpty() && (atk.abi & AB_MASSIVE) != 0)
				ans = (int) (ans * EUnit.OrbHandler.getOrbMassive(atk, sharedTraits, basis.b.t()));

			if (!sharedTraits.isEmpty() && (atk.abi & AB_MASSIVES) != 0)
				ans = (int) (ans * basis.b.t().getMASSIVESATK(sharedTraits));
		}

		if (isBase)
			ans = (int) (ans * (1 + atk.getProc().ATKBASE.mult / 100.0));

		if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_WITCH)) && (atk.abi & AB_WKILL) > 0)
			ans = (int) (ans * basis.b.t().getWKAtk(basis.isBanned(Data.C_WKILL)));

		if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_EVA)) && (atk.abi & AB_EKILL) > 0)
			ans = (int) (ans * basis.b.t().getEKAtk(basis.isBanned(Data.C_EKILL)));

		if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_BARON)) && (atk.abi & AB_BAKILL) > 0)
			ans = (int) (ans * 1.6);

		if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_BEAST)) && atk.getProc().BSTHUNT.type.active)
			ans = (int) (ans * 2.5);

		if (traits.contains(BCTraits.get(TRAIT_SAGE)) && (atk.abi & AB_SKILL) > 0)
			ans = (int) (ans * SUPER_SAGE_HUNTER_ATTACK);

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
	protected int traitType() {
		return 1;
	}

	@Override
	public void damaged(AttackAb atk) {
		hit = 2;
		super.damaged(atk);
	}

	@Override
	public float getResistValue(AttackAb atk, String procName, int procResist) {
		float ans = 1f - procResist / 100f;

		boolean canBeApplied = false;

		for (int i = 0; i < SUPER_SAGE_RESIST_TYPE.length; i++) {
			if (procName.equals(SUPER_SAGE_RESIST_TYPE[i])) {
				canBeApplied = true;

				break;
			}
		}

		if ((atk.abi & AB_SKILL) == 0 && traits.contains(BCTraits.get(TRAIT_SAGE)) && canBeApplied) {
			ans *= (1f - SUPER_SAGE_RESIST);
		}

		return ans;
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

		if (health > 0)
			status[P_BOUNTY][0] = 0;
	}

	@Override
	protected void onLastBreathe() {
	}
}
