package common.battle.data;

import common.util.unit.Form;

public interface MaskUnit extends MaskEntity {
	int getBack();

	int getFront();

	@Override
	Form getPack();

	int getPrice();

	int getRespawn();

	default int getFirstRespawn() {
		return 0;
	}

	PCoin getPCoin();

	void improve(int[] type, int mod);

	MaskUnit clone();

	int getLimit();
}
