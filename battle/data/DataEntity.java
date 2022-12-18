package common.battle.data;

import common.io.json.JsonClass;
import common.io.json.JsonClass.NoTag;
import common.io.json.JsonField;
import common.pack.Identifier;
import common.pack.SortedPackSet;
import common.util.Data;
import common.util.pack.Soul;
import common.util.unit.Trait;

@JsonClass(noTag = NoTag.LOAD)
public abstract class DataEntity extends Data implements MaskEntity {

	public int hp, hb, speed, range, tba, abi, width, loop = -1, will;

	public Identifier<Soul> death;
	@JsonField(generic = Trait.class, alias = Identifier.class)
	public SortedPackSet<Trait> traits = new SortedPackSet<>();

	@Override
	public int getAbi() {
		return abi;
	}

	@Override
	public int getAtkLoop() {
		return loop;
	}

	@Override
	public Identifier<Soul> getDeathAnim() {
		return death;
	}

	@Override
	public SortedPackSet<Trait> getTraits() {
		return traits;
	}

	@Override
	public int getHb() {
		return hb;
	}

	@Override
	public int getHp() {
		return hp;
	}

	@Override
	public int getRange() {
		return range;
	}

	@Override
	public int getSpeed() {
		return speed;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getWill() {
		return will;
	}
}
