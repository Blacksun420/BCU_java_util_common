package common.battle.entity;

import common.CommonStatic;
import common.battle.StageBasis;
import common.battle.Treasure;
import common.battle.attack.AtkModelEnemy;
import common.battle.attack.AtkModelUnit;
import common.battle.attack.AttackAb;
import common.battle.data.MaskAtk;
import common.battle.data.MaskUnit;
import common.battle.data.Orb;
import common.battle.data.PCoin;
import common.pack.SortedPackSet;
import common.pack.UserProfile;
import common.util.BattleObj;
import common.util.Data;
import common.util.anim.EAnimU;
import common.util.unit.Form;
import common.util.unit.Level;
import common.util.unit.Trait;

import java.util.List;

public class EUnit extends Entity {

	private static final SortedPackSet<Trait> blank = new SortedPackSet<>(0);

	public static class OrbHandler extends BattleObj {

		protected static float getOrb(double mult, AttackAb atk, SortedPackSet<Trait> traits, Treasure t) {
			if(atk.origin.model instanceof AtkModelUnit)
				return ((EUnit) ((AtkModelUnit) atk.origin.model).e).getOrb(mult, atk.trait, traits, t);
			return ((EUnit) ((AtkModelUnit)atk.model).e).getOrb(mult, atk.trait, traits, t);
		}

		protected static int getOrbAtk(AttackAb atk, EEnemy en) {
			if (atk.matk == null || !(atk.origin.model instanceof AtkModelUnit))
				return 0;
			// Warning : Eunit.e became public now
			EUnit unit = (EUnit)((AtkModelUnit) atk.origin.model).e;
			return unit.getOrb(en.traits, atk.matk.getAtk(), true);
		}
	}

	public final int lvl;
	public final int[] index;

	protected final Level level;
	/**
	 * Last position where entity moved without interruption
	 */
	public float lastPosition;

	public EUnit(StageBasis b, MaskUnit de, EAnimU ea, float d0, int layer0, int layer1, Level level, PCoin pc, int[] index, boolean isBase) {
		super(b, de, ea, d0, pc, level);
		layer = layer0 == layer1 ? layer0 : layer0 + (int) (b.r.nextFloat() * (layer1 - layer0 + 1));
		traits = de.getTraits();
		lvl = level.getTotalLv();
		this.index = index;
		this.isBase = isBase;
		if (isBase && !b.isBanned(C_BASE)) {
			maxH *= (100 + b.elu.getInc(C_BASE)) * 0.01;
			health = maxH;
		}

		this.level = level;
	}

	//used for waterblast
	public EUnit(StageBasis b, MaskUnit de, EAnimU ea, float d0) {
		super(b, de, ea, d0, null, null);
		layer = de.getFront() + (int) (b.r.nextFloat() * (de.getBack() - de.getFront() + 1));
		traits = de.getTraits();
		this.index = null;

		lvl = 1;
		health = maxH = (int) (health * b.b.t().getCannonMagnification(BASE_WALL, BASE_WALL_MAGNIFICATION) / 100.0);
		level = null;
	}

	@Override
	public void added(int d, float p) {
		super.added(d,p);
		lastPosition = p;
	}

	@Override
	public void kill(boolean glass) {
		super.kill(glass);
		if (!glass && status.money != 0)
			basis.money -= (status.money / 100.0) * (index != null ? basis.elu.price[index[0]][index[1]] : ((MaskUnit)data).getPrice());
		if (index != null && basis.elu.smnd[index[0]][index[1]] == this)
			basis.elu.smnd[index[0]][index[1]] = null;
 	}

	@Override
	public void update() {
		super.update();
		traits = status.curse == 0 && status.seal == 0 ? data.getTraits() : blank;
		if (kbTime == 0)
			lastPosition = pos;
	}

	@Override
	public float calcDamageMult(int dmg, Entity e, MaskAtk matk) {
		float ans = super.calcDamageMult(dmg, e, matk);
		if (ans == 0)
			return 0;
		if (e instanceof EEnemy) {
			if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_WITCH)) && (e.getAbi() & AB_WKILL) > 0)
				ans *= basis.b.t().getWKDef();
			if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_EVA)) && (e.getAbi() & AB_EKILL) > 0)
				ans *= basis.b.t().getEKDef();
			if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_BARON)) && (e.getAbi() & AB_BAKILL) > 0)
				ans *= 0.7;
			if (traits.contains(UserProfile.getBCData().traits.get(TRAIT_BEAST)) && matk.getProc().BSTHUNT.type.active)
				ans *= 0.6;
		}
		return ans;
	}

	@Override
	public void damaged(AttackAb atk) {
		if (atk.trait.contains(BCTraits.get(TRAIT_BEAST))) {
			Proc.BSTHUNT beastDodge = getProc().BSTHUNT;
			if (beastDodge.prob > 0 && (atk.dire != getDire())) {
				if (status.wild == 0 && (beastDodge.prob == 100 || basis.r.nextFloat() * 100 < beastDodge.prob)) {
					status.wild = beastDodge.time;
					anim.getEff(P_IMUATK);
				}
				if (status.wild > 0) {
					damageTaken += atk.atk;
					sumDamage(atk.atk, true);
					return;
				}
			}
		}
		super.damaged(atk);
	}

	@Override
	protected void sumDamage(int atk, boolean raw) {
		if (index != null && CommonStatic.getConfig().rawDamage == raw)
			basis.totalDamageTaken[index[0]][index[1]] += atk;
	}

	@Override
	public float getResistValue(AttackAb atk, boolean SageRes, double procResist) {
		float ans = (float) ((100f - procResist) / 100f);

		if (SageRes && atk.trait.contains(BCTraits.get(TRAIT_SAGE)) && (getAbi() & AB_SKILL) != 0)
			ans *= SUPER_SAGE_HUNTER_RESIST;
		return ans;
	}

	@Override
	protected int getDamage(AttackAb atk, int ans) {
		ans = super.getDamage(atk, ans);
		if (atk.model instanceof AtkModelEnemy) {
			SortedPackSet<Trait> sharedTraits = traits.inCommon(atk.trait);
			boolean isAntiTraited = targetTraited(atk.trait);
			sharedTraits.addIf(atk.trait, t -> !t.BCTrait() && ((t.targetType && isAntiTraited) || t.others.contains((Form)data.getPack())));
			if (!sharedTraits.isEmpty()) {
				if (status.curse == 0 && getProc().DEFINC.mult != 0)
					ans *= basis.b.t().getDEF(getProc().DEFINC.mult, atk.trait, sharedTraits, ((MaskUnit) data).getOrb(), level, basis.isBanned(getProc().DEFINC.mult < 400 ? C_GOOD : C_RESIST));
				if (atk.attacker.status.curse == 0 && atk.attacker.getProc().DMGINC.mult != 0)
					ans *= atk.attacker.getProc().DMGINC.mult / 100.0;
			}
			if (atk.trait.contains(UserProfile.getBCData().traits.get(TRAIT_WITCH)) && (getAbi() & AB_WKILL) > 0)
				ans *= basis.b.t().getWKDef(basis.isBanned(C_WKILL));
			if (atk.trait.contains(UserProfile.getBCData().traits.get(TRAIT_EVA)) && (getAbi() & AB_EKILL) > 0)
				ans *= basis.b.t().getEKDef(basis.isBanned(C_EKILL));
			if (atk.trait.contains(UserProfile.getBCData().traits.get(TRAIT_BARON)) && (getAbi() & AB_BAKILL) > 0)
				ans *= 0.7;
			if (atk.trait.contains(UserProfile.getBCData().traits.get(Data.TRAIT_BEAST)) && getProc().BSTHUNT.type.active)
				ans *= 0.6; //Not sure
			if (atk.trait.contains(UserProfile.getBCData().traits.get(Data.TRAIT_SAGE)) && (getAbi() & AB_SKILL) > 0)
				ans = (int) (ans * SUPER_SAGE_HUNTER_HP);
		}
		// Perform orb
		ans = getOrb(atk.trait, ans, false);

		if(basis.canon.base > 0)
			ans = (int) (ans * basis.b.t().getBaseMagnification(basis.canon.base, atk.trait));
		return critCalc((getAbi() & AB_METALIC) != 0, ans, atk);
	}

	@Override
	protected void processProcs0(AttackAb atk, int dmg) {
		Proc.CDSETTER cd = atk.getProc().CDSETTER;
		if (cd.prob > 0 && cd.slot == 10 && index != null && index[1] < 5)
			basis.changeUnitCooldown(cd.amount, index[0] * 5 + index[1], cd.type);
		super.processProcs0(atk, dmg);
	}

	@Override
	protected float getLim() {
		return Math.max(0, basis.st.len - pos - ((MaskUnit) data).getLimit());
	}

	@Override
	protected float updateMove(float extmov) {
		if (status.slow == 0)
			extmov += (float)(data.getSpeed() * basis.elu.getInc(C_SPE) / 50) / 4f;
		return super.updateMove(extmov);
	}

	@Override
	protected float getMov(float extmov) {
		if (status.slow == 0)
			extmov = extmov + (float)(data.getSpeed() * basis.elu.getInc(C_SPE) / 50) / 4f;
		return super.getMov(extmov);
	}

	private int getOrb(SortedPackSet<Trait> trait, int matk, boolean atk) {
		Orb orb = ((MaskUnit) data).getOrb();
		if (orb == null || level.getOrbs() == null)
			return atk ? 0 : matk;
		int ans = atk ? 0 : matk;
		for (int[] line : level.getOrbs()) {
			int ORB = atk ? ORB_ATK : ORB_RES;
			if (line.length == 0 || line[ORB_TYPE] != ORB)
				continue;
			List<Trait> orbType = Trait.convertOrb(line[ORB_TRAIT]);
			boolean orbValid = false;
			for (Trait orbT : orbType)
				if (trait.contains(orbT)) {
					orbValid = true;
					break;
				}
			if (orbValid) {
				if (atk)
					ans += orb.getAtk(line[ORB_GRADE], matk);
				else
					ans = orb.getRes(line[ORB_GRADE], ans);
			}
		}
		return ans;
	}

	private float getOrb(double mult, SortedPackSet<Trait> eTraits, SortedPackSet<Trait> traits, Treasure t) {
		final int ORB_LV = mult < 500 && mult > 100 ? mult < 300 ? ORB_STRONG : ORB_MASSIVE : -1;
		final float[] ORB_MULTIS = ORB_LV == -1 ? new float[0] : ORB_LV == ORB_STRONG ? ORB_STR_ATK_MULTI : ORB_MASSIVE_MULTI;

		float ini = 1;
		if (!traits.isEmpty())
			ini = (float) ((mult/100f) + (ORB_LV == ORB_STRONG ? 0.3f : mult > 100 ? 1f : 0f) / 3 * t.getFruit(traits));

		Orb orbs = ((MaskUnit)data).getOrb();
		if(orbs != null && level.getOrbs() != null) {
			int[][] levelOrbs = level.getOrbs();
			for (int[] lvOrb : levelOrbs)
				if (lvOrb.length == ORB_TOT && lvOrb[ORB_TYPE] == ORB_LV) {
					List<Trait> orbType = Trait.convertOrb(lvOrb[ORB_TRAIT]);
					for (Trait orbTr : orbType)
						if (eTraits.contains(orbTr)) {
							ini += ORB_MULTIS[lvOrb[ORB_GRADE]];
							break;
						}
				}
		}
		if (ini == 1 || ORB_LV == -1)
			return ini;
		float com = 1 + basis.elu.getInc(ORB_LV == ORB_STRONG ? C_GOOD : C_MASSIVE) * 0.01f;
		return ini * com;
	}

	@Override
	protected void onLastBreathe() {
		super.onLastBreathe();
		basis.notifyUnitDeath();
	}

	@Override
	public double buff(int lv) {
		return lv + lvl;
	}
}
