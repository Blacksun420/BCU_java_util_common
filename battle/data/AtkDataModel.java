package common.battle.data;

import common.io.json.JsonClass;
import common.io.json.JsonClass.NoTag;
import common.io.json.JsonClass.RType;
import common.io.json.JsonDecoder;
import common.io.json.JsonField;
import common.pack.Identifier;
import common.system.BasedCopable;
import common.util.Data;
import common.util.stage.Music;
import common.util.unit.Trait;

import java.util.ArrayList;

@JsonClass(read = RType.FILL, noTag = NoTag.LOAD)
public class AtkDataModel extends Data implements MaskAtk, BasedCopable<AtkDataModel, CustomEntity> {

	@JsonField(block = true)
	public final CustomEntity ce;
	public String str = "";
	public int atk, pre = 1, ld0, ld1, targ = TCH_N, count = -1, dire = 1, alt = 0, move = 0;
	public boolean range = true;
	@JsonField(io = JsonField.IOType.R)
	public boolean specialTrait = false; //Special trait makes attacks that ignore traits consider traits, and attacks that don't do
	@JsonField(generic = Trait.class, alias = Identifier.class)
	public ArrayList<Trait> traits = new ArrayList<>(); //Gives attacks their own typings. SpecialTrait but better lol

	@JsonField
	public Identifier<Music> audio, audio1;

	@JsonField
	public Proc proc;

	public AtkDataModel(CustomEntity ent) {
		ce = ent;
		str = ce.getAvailable(str);
		proc = Proc.blank();
	}

	protected AtkDataModel(CustomEntity ene, AtkDataModel adm) {
		ce = ene;
		str = ce.getAvailable(adm.str);
		atk = adm.atk;
		pre = adm.pre;
		ld0 = adm.ld0;
		ld1 = adm.ld1;
		range = adm.range;
		traits = new ArrayList<>(adm.traits);
		dire = adm.dire;
		count = adm.count;
		targ = adm.targ;
		alt = adm.alt;
		move = adm.move;
		proc = adm.proc.clone();
		audio = adm.audio;
		audio1 = adm.audio1;
	}

	protected AtkDataModel(CustomEntity ene, MaskEntity me, int i) {
		ce = ene;
		str = ce.getAvailable("copied");
		MaskAtk am = me.getAtkModel(0, i);
		proc = am.getProc().clone();
		ld0 = am.getShortPoint();
		ld1 = am.getLongPoint();
		pre = am.getPre();
		atk = am.getAtk();
		range = am.isRange();
		dire = am.getDire();
		count = am.loopCount();
		targ = am.getTarget();
		alt = am.getAltAbi();
		move = am.getMove();
	}

	@Override
	public AtkDataModel clone() {
		return new AtkDataModel(ce, this);
	}

	@Override
	public AtkDataModel copy(CustomEntity nce) {
		return new AtkDataModel(nce, this);
	}

	@Override
	public int getAltAbi() {
		return alt;
	}

	@Override
	public int getAtk() {
		return atk;
	}

	@Override
	public int getPre() {
		return pre;
	}

	@Override
	public int getDire() {
		return dire;
	}

	@Override
	public int getLongPoint() {
		return ld1;
	}

	@Override
	public int getMove() {
		return move;
	}

	@Override
	public ArrayList<Trait> getATKTraits() { return traits; }

	@Override
	public Proc getProc() {
		if (ce.rep != this && ce.common)
			return ce.rep.getProc();
		return proc;
	}

	@Override
	public int getShortPoint() {
		return ld0;
	}

	@Override
	public int getTarget() {
		return targ;
	}

	@Override
	public boolean isRange() {
		return range;
	}

	@Override
	public int loopCount() {
		return count;
	}

	@Override
	public String toString() {
		return str;
	}

	@Override
	public boolean isLD() {
		return (ld0 > 0 || ld1 < 0) && !isOmni();
	}

	@Override
	public boolean isOmni() {
		return ld0 * ld1 < 0 || (ld0 == 0 && ld1 > 0) || (ld0 < 0 && ld1 == 0);
	}

	@Override
	public Identifier<Music> getAudio(boolean sec) {
		return sec ? audio1 : audio;
	}

	@JsonDecoder.OnInjected
	public void onInjected() {
		if (proc == null)
			proc = Proc.blank();
	}

}
