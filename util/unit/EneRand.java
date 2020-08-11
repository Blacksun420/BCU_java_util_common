package common.util.unit;

import java.util.Set;
import java.util.TreeSet;

import common.battle.StageBasis;
import common.battle.entity.EEnemy;
import common.io.InStream;
import common.pack.PackData.Identifier;
import common.system.VImg;
import common.util.EREnt;
import common.util.EntRand;
import common.util.Res;

public class EneRand extends EntRand<Identifier<AbEnemy>> implements AbEnemy {

	public final Identifier<AbEnemy> id;

	public String name = "";

	public EneRand(Identifier<AbEnemy> ID) {
		id = ID;
	}

	public void fillPossible(Set<Enemy> se, Set<EneRand> sr) {
		sr.add(this);
		for (EREnt<Identifier<AbEnemy>> e : list) {
			AbEnemy ae = e.ent.get();
			if (ae instanceof Enemy)
				se.add((Enemy) ae);
			if (ae instanceof EneRand) {
				EneRand er = (EneRand) ae;
				if (!sr.contains(er))
					er.fillPossible(se, sr);
			}
		}
	}

	@Override
	public EEnemy getEntity(StageBasis sb, Object obj, double mul, double mul2, int d0, int d1, int m) {
		sb.rege.add(this);
		return get(getSelection(sb, obj), sb, obj, mul, mul2, d0, d1, m);
	}

	@Override
	public VImg getIcon() {
		return Res.ico[0][0];
	}

	@Override
	public Identifier<AbEnemy> getID() {
		return id;
	}

	@Override
	public Set<Enemy> getPossible() {
		Set<Enemy> te = new TreeSet<>();
		fillPossible(te, new TreeSet<EneRand>());
		return te;
	}

	@Override
	public String toString() {
		return id.id + " - " + name + " (" + id.pack + ")";
	}

	public void zread(InStream is) {
		int ver = getVer(is.nextString());
		if (ver >= 400)
			zread$000400(is);
	}

	private EEnemy get(EREnt<Identifier<AbEnemy>> x, StageBasis sb, Object obj, double mul, double mul2, int d0, int d1,
			int m) {
		return x.ent.get().getEntity(sb, obj, x.multi * mul / 100, x.multi * mul2 / 100, d0, d1, m);
	}

	private void zread$000400(InStream is) {
		name = is.nextString();
		type = is.nextInt();
		int n = is.nextInt();
		for (int i = 0; i < n; i++) {
			EREnt<Identifier<AbEnemy>> ere = new EREnt<>();
			list.add(ere);
			ere.ent = Identifier.parseInt(is.nextInt(), EneRand.class);
			ere.multi = is.nextInt();
			ere.share = is.nextInt();
		}
	}

}
