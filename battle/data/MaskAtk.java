package common.battle.data;

import common.pack.Identifier;
import common.util.BattleStatic;
import common.util.Data;
import common.util.Data.Proc;
import common.util.stage.Music;
import common.util.unit.Trait;

import java.util.ArrayList;

public interface MaskAtk extends BattleStatic {

	default int getAltAbi() {
		return 0;
	}

	int getAtk();

	default int getDire() {
		return 1;
	}

	int getLongPoint();

	default int getMove() {
		return 0;
	}

	default ArrayList<Trait> getATKTraits() {
		return new ArrayList<>();
	};

	Proc getProc();

	int getShortPoint();

	default int getTarget() {
		return Data.TCH_N;
	}

	boolean isOmni();

	boolean isRange();

	default int loopCount() {
		return -1;
	}

	default Identifier<Music> getAudio(boolean sec) {
		return null;
	}
}
