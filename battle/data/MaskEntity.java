package common.battle.data;

import common.pack.Identifier;
import common.util.Animable;
import common.util.BattleStatic;
import common.util.Data;
import common.util.Data.Proc;
import common.util.anim.AnimU;
import common.util.anim.AnimU.UType;
import common.util.pack.Soul;
import common.util.unit.Trait;

import java.util.ArrayList;

public interface MaskEntity extends BattleStatic {

	int allAtk(int atk);

	int getAbi();

	Proc getAllProc();

	/**
	 * get the attack animation length
	 */
	default int getAnimLen(int atk) {
		return getPack().anim.getAtkLen(atk);
	}

	int getAtkCount(int atk);

	int getAtkLoop();

	MaskAtk getAtkModel(int atk, int ind);

	MaskAtk[] getAtks(int atk);

	default int getAtkTypeCount() {
		return 1;
	}

	default MaskAtk[][] getAllAtks() {
		return new MaskAtk[][]{getAtks(0)};
	}

	default int getShare(int atk) {
		return 1;
	}

	default AtkDataModel[] getSpAtks() {
		return new AtkDataModel[0];
	}

	Identifier<Soul> getDeathAnim();

	ArrayList<Trait> getTraits();

	int getHb();

	int getHp();

	/**
	 * get the attack period
	 */
	int getItv(int atk);

	/**
	 * get the Enemy/Form this data represents
	 */
	Animable<AnimU<?>, UType> getPack();

	int getPost(int atk);

	Proc getProc();

	int getRange();

	MaskAtk getRepAtk();

	default AtkDataModel getResurrection() {
		return null;
	}

	default AtkDataModel getRevenge() {
		return null;
	}

	default AtkDataModel getCounter() {
		return null;
	}

	default AtkDataModel getGouge() {
		return null;
	}

	default AtkDataModel getResurface() {
		return null;
	}

	default AtkDataModel getRevive() {
		return null;
	}

	default AtkDataModel getEntry() {
		return null;
	}

	int getSpeed();

	int getWill();

	/**
	 * get waiting time
	 */
	int getTBA();

	default int getTouch() {
		return Data.TCH_N;
	}

	int getWidth();

	boolean isLD();

	boolean isOmni();

	boolean isRange(int atk);

	int touchBase();

	default void animChanged(int del) {

	}

}
