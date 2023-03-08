package common.battle.entity;

import common.battle.StageBasis;
import common.battle.Treasure;
import common.battle.attack.AtkModelEnemy;
import common.battle.attack.AtkModelUnit;
import common.battle.attack.AttackAb;
import common.battle.attack.AttackWave;
import common.battle.data.MaskAtk;
import common.battle.data.MaskUnit;
import common.battle.data.Orb;
import common.battle.data.PCoin;
import common.pack.SortedPackSet;
import common.pack.UserProfile;
import common.util.BattleObj;
import common.util.Data;
import common.util.anim.EAnimU;
import common.util.unit.Level;
import common.util.unit.Trait;

import java.util.List;

@SuppressWarnings("ForLoopReplaceableByForEach")
public class EUnit extends Entity {

	private static final SortedPackSet<Trait> blankSet = new SortedPackSet<>();

	public static class OrbHandler extends BattleObj {
		protected static int getOrbAtk(AttackAb atk, EEnemy en) {
			if (atk.matk == null) {
				return 0;
			}

			if (atk.origin.model instanceof AtkModelUnit) {
				// Warning : Eunit.e became public now
				EUnit unit = (EUnit) ((AtkModelUnit) atk.origin.model).e;

				return unit.getOrbAtk(en.traits, atk.matk);
			}

			return 0;
		}

		protected static double getOrbMassive(AttackAb atk, SortedPackSet<Trait> traits, Treasure t) {
			if(atk.origin.model instanceof AtkModelUnit) {
				return ((EUnit) ((AtkModelUnit) atk.origin.model).e).getOrbMassive(atk.trait, traits, t);
			}

			return ((EUnit) ((AtkModelUnit)atk.model).e).getOrbMassive(atk.trait, traits, t);
		}

		protected static double getOrbGood(AttackAb atk, SortedPackSet<Trait> traits, Treasure t) {
			if(atk.origin.model instanceof AtkModelUnit) {
				return ((EUnit) ((AtkModelUnit) atk.origin.model).e).getOrbGood(atk.trait, traits, t);
			}

			return ((EUnit) ((AtkModelUnit)atk.model).e).getOrbGood(atk.trait, traits, t);
		}
	}

	public final int lvl;
	public final int[] index;

	protected final Level level;

	public EUnit(StageBasis b, MaskUnit de, EAnimU ea, double d0, int layer0, int layer1, Level level, PCoin pc, int[] index, boolean isBase) {
		super(b, de, ea, d0, b.b.t().getAtkMulti(), b.b.t().getDefMulti(), pc, level);
		layer = layer0 == layer1 ? layer0 : layer0 + (int) (b.r.nextDouble() * (layer1 - layer0 + 1));
		traits = de.getTraits();
		lvl = level.getLv() + level.getPlusLv();
		this.index = index;
		this.isBase = isBase;

		this.level = level;
	}

	//used for waterblast
	public EUnit(StageBasis b, MaskUnit de, EAnimU ea, double d0) {
		super(b, de, ea, d0, b.b.t().getAtkMulti(), b.b.t().getDefMulti(), null, null);
		layer = de.getFront() + (int) (b.r.nextDouble() * (de.getBack() - de.getFront() + 1));
		traits = de.getTraits();
		this.index = null;

		lvl = 1;
		health = maxH = (int) (health * b.b.t().getCannonMagnification(BASE_WALL, BASE_WALL_MAGNIFICATION) / 100.0);
		level = null;
	}

	@Override
	public void kill(boolean atk) {
		super.kill(atk);
		if (status.money != 0)
			basis.money -= (status.money / 100.0) * (index != null ? basis.elu.price[index[0]][index[1]] : ((MaskUnit)data).getPrice());
 	}

	@Override
	public void update() {
		super.update();
		traits = status.curse == 0 && status.seal == 0 ? data.getTraits() : blankSet;
	}

	@Override
	public float calcDamageMult(int dmg, Entity e, MaskAtk matk) {
		float ans = super.calcDamageMult(dmg, e, matk);
		if (ans == 0)
			return 0;
		if (e instanceof EEnemy) {
			SortedPackSet<Trait> sharedTraits = traits.inCommon(matk.getATKTraits());
			boolean isAntiTraited = targetTraited(matk.getATKTraits());
			for (Trait t : traits) {
				if (t.BCTrait() || sharedTraits.contains(t))
					continue;
				if ((t.targetType && isAntiTraited) || t.others.contains(((MaskUnit)e.data).getPack()))
					sharedTraits.add(t);
			}

			if (!sharedTraits.isEmpty()) {
				if (status.curse == 0) {
					if ((getAbi() & AB_GOOD) != 0)
						ans *= 1.5;
					if ((getAbi() & AB_MASSIVE) != 0)
						ans *= 3;
					if ((getAbi() & AB_MASSIVES) != 0)
						ans *= 5;
				}
				if (e.status.curse == 0) {
					if ((e.getAbi() & AB_GOOD) > 0)
						ans /= 2;
					if ((e.getAbi() & AB_RESIST) > 0)
						ans /= 4;
					if ((e.getAbi() & AB_RESISTS) > 0)
						ans /= 6;
				}
			}
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
				if (status.wild == 0 && (beastDodge.prob == 100 || basis.r.nextDouble() * 100 < beastDodge.prob)) {
					status.wild = beastDodge.time;
					anim.getEff(P_IMUATK);
				}

				if (status.wild > 0) {
					damageTaken += atk.atk;

					if(index != null) {
						basis.totalDamageTaken[index[0]][index[1]] += atk.atk;
					}

					return;
				}
			}
		}

		super.damaged(atk);

		if(index != null) {
			basis.totalDamageTaken[index[0]][index[1]] += atk.atk;
		}
	}

	@Override
	protected int getDamage(AttackAb atk, int ans) {
		if (atk instanceof AttackWave && atk.waveType == WT_MINI) {
			ans = (int) ((double) ans * atk.getProc().MINIWAVE.multi / 100.0);
		}
		if (atk.model instanceof AtkModelEnemy) {
			SortedPackSet<Trait> sharedTraits = traits.inCommon(atk.trait);
			boolean isAntiTraited = targetTraited(atk.trait);
			for (Trait t : atk.trait) {
				if (t.BCTrait() || sharedTraits.contains(t))
					continue;
				if ((t.targetType && isAntiTraited) || t.others.contains(((MaskUnit)data).getPack()))
					sharedTraits.add(t);
			}
			if (!sharedTraits.isEmpty()) {
				if (status.curse == 0) {
					if ((getAbi() & AB_GOOD) != 0)
						ans *= basis.b.t().getGOODDEF(atk.trait, sharedTraits, ((MaskUnit) data).getOrb(), level);
					if ((getAbi() & AB_RESIST) != 0)
						ans *= basis.b.t().getRESISTDEF(atk.trait, sharedTraits, ((MaskUnit) data).getOrb(), level);
					if ((getAbi() & AB_RESISTS) != 0)
						ans *= basis.b.t().getRESISTSDEF(sharedTraits);
				}
				if (atk.attacker.status.curse == 0) {
					if ((atk.abi & AB_GOOD) != 0)
						ans *= 1.5;
					if ((atk.abi & AB_MASSIVE) != 0)
						ans *= 3;
					if ((atk.abi & AB_MASSIVES) != 0)
						ans *= 5;
				}
			}
			if (atk.trait.contains(UserProfile.getBCData().traits.get(TRAIT_WITCH)) && (getAbi() & AB_WKILL) > 0)
				ans *= basis.b.t().getWKDef();
			if (atk.trait.contains(UserProfile.getBCData().traits.get(TRAIT_EVA)) && (getAbi() & AB_EKILL) > 0)
				ans *= basis.b.t().getEKDef();
			if (atk.trait.contains(UserProfile.getBCData().traits.get(TRAIT_BARON)) && (getAbi() & AB_BAKILL) > 0)
				ans *= 0.7;
			if (atk.trait.contains(UserProfile.getBCData().traits.get(Data.TRAIT_BEAST)) && getProc().BSTHUNT.type.active)
				ans *= 0.6; //Not sure
		}
		if (isBase)
			ans *= 1 + atk.getProc().ATKBASE.mult / 100.0;
		ans = critCalc((getAbi() & AB_METALIC) != 0, ans, atk);

		// Perform orb
		ans = getOrbRes(atk.trait, ans);

		return ans;
	}

	@Override
	protected double getLim() {
		return Math.max(0, basis.st.len - pos - ((MaskUnit) data).getLimit());
	}

	@Override
	protected double updateMove(double extmov) {
		if (status.slow == 0)
			extmov += data.getSpeed() * basis.b.getInc(C_SPE) / 200.0;
		return super.updateMove(extmov);
	}

	@Override
	protected double getMov(double extmov) {
		if (status.slow == 0)
			extmov += data.getSpeed() * basis.b.getInc(C_SPE) / 200.0;
		return super.getMov(extmov);
	}

	private int getOrbAtk(SortedPackSet<Trait> trait, MaskAtk matk) {
		Orb orb = ((MaskUnit) data).getOrb();

		if (orb == null || level.getOrbs() == null) {
			return 0;
		}

		int ans = 0;

		for (int[] line : level.getOrbs()) {
			if (line.length == 0)
				continue;

			if (line[ORB_TYPE] != Data.ORB_ATK)
				continue;

			List<Trait> orbType = Trait.convertOrb(line[ORB_TRAIT]);

			boolean orbValid = false;

			for(int i = 0; i < orbType.size(); i++) {
				if (trait.contains(orbType.get(i))) {
					orbValid = true;

					break;
				}
			}

			if (!orbValid)
				continue;

			ans += orb.getAtk(line[ORB_GRADE], matk);
		}

		return ans;
	}

	private int getOrbRes(SortedPackSet<Trait> trait, int atk) {
		Orb orb = ((MaskUnit) data).getOrb();

		if (orb == null || level.getOrbs() == null)
			return atk;

		int ans = atk;

		for (int[] line : level.getOrbs()) {
			if (line.length == 0 || line[ORB_TYPE] != Data.ORB_RES)
				continue;

			List<Trait> orbType = Trait.convertOrb(line[ORB_TRAIT]);

			boolean orbValid = false;

			for(int i = 0; i < orbType.size(); i++) {
				if (trait.contains(orbType.get(i))) {
					orbValid = true;

					break;
				}
			}

			if (!orbValid)
				continue;

			ans = orb.getRes(line[ORB_GRADE], ans);
		}

		return ans;
	}

	private double getOrbMassive(SortedPackSet<Trait> eTraits, SortedPackSet<Trait> traits, Treasure t) {
		double ini = 1;

		if (!traits.isEmpty())
			ini = 3 + 1.0 / 3 * t.getFruit(traits);

		Orb orbs = ((MaskUnit)data).getOrb();

		if(orbs != null && level.getOrbs() != null) {
			int[][] levelOrbs = level.getOrbs();

			for(int i = 0; i < levelOrbs.length; i++) {
				if (levelOrbs[i].length < ORB_TOT)
					continue;

				if (levelOrbs[i][ORB_TYPE] == ORB_MASSIVE) {
					List<Trait> orbType = Trait.convertOrb(levelOrbs[i][ORB_TRAIT]);

					for(int j = 0; j < orbType.size(); j++) {
						if (eTraits.contains(orbType.get(j))) {
							ini += ORB_MASSIVE_MULTI[levelOrbs[i][ORB_GRADE]];

							break;
						}
					}
				}
			}
		}

		if (ini == 1)
			return ini;

		double com = 1 + t.b.getInc(C_MASSIVE) * 0.01;

		return ini * com;
	}

	private double getOrbGood(SortedPackSet<Trait> eTraits, SortedPackSet<Trait> traits, Treasure t) {
		double ini = 1;

		if (!traits.isEmpty())
			ini = 1.5 * (1 + 0.2 / 3 * t.getFruit(traits));

		Orb orbs = ((MaskUnit)data).getOrb();

		if(orbs != null && level.getOrbs() != null) {
			int[][] levelOrbs = level.getOrbs();

			for (int i = 0; i < levelOrbs.length; i++) {
				if (levelOrbs[i].length < ORB_TOT)
						continue;

				if (levelOrbs[i][ORB_TYPE] == ORB_STRONG) {
					List<Trait> orbType = Trait.convertOrb(levelOrbs[i][ORB_TRAIT]);

					for(int j = 0; j < orbType.size(); j++) {
						if (eTraits.contains(orbType.get(j))) {
							ini += ORB_STR_ATK_MULTI[levelOrbs[i][ORB_GRADE]];

							break;
						}
					}
				}
			}
		}

		if (ini == 1)
			return ini;

		double com = 1 + t.b.getInc(C_GOOD) * 0.01;
		return ini * com;
	}

	@Override
	protected void onLastBreathe() {
		basis.notifyUnitDeath();
	}

	@Override
	public double buff(int lv) {
		return lv + lvl;
	};
}
