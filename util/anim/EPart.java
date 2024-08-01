package common.util.anim;

import common.CommonStatic;
import common.pack.Context.ErrType;
import common.system.P;
import common.system.fake.FakeGraphics;
import common.system.fake.FakeImage;
import common.system.fake.FakeTransform;
import common.util.ImgCore;

public class EPart extends ImgCore implements Comparable<EPart> {

	public static class EBase {
		private final MaModel model;
		private final AnimI<?, ?> a;
		private final EPart[] ent;
		public boolean EWarp, pvt;
		boolean flipped;

		public EBase(MaModel mm, AnimI<?, ?> aa, EPart[] ents) {
			model = mm;
			a = aa;
			ent = ents;
		}

		public void revert() {
			flipped = !flipped;
			for (EPart e : ent) {
				if (e.fa == null) {
					e.sca.x *= -1;
					e.piv.x *= -1;
				}
				e.angle *= -1;
			}
		}
	}

	private final String name;
	public final EBase b;
	private final int[] args;
	private final int ind;
	private EPart fa, para;
	private int id, img;
	private P pos = new P(0, 0), piv = new P(0, 0), sca = new P(0, 0);
	private int z, glow, extType; // extType - 0 : Slow, 1 : Curse
	private float angle, opacity, extendX, extendY, gsca;
	private int hf, vf;

	EPart(EBase base, int[] part, String str, int i) {
		b = base;
		args = part;
		name = str;
		ind = i;
		setValue();
	}

	/**
	 * Gets this component's parent
	 * @return this component's parent, or -1 if it doesn't has any
	 */
	public int getPar() {
		if (fa == null)
			return -1;

		for (int i = 0; i < b.ent.length; i++)
			if (fa == b.ent[i])
				return i;
		return -1;
	}

	public void alter(int m, float v) {
		if (m == 0)
			if (v < b.ent.length && v >= 0 && v != ind)
				fa = b.ent[(int) v];
			else
				fa = null;
		else if (m == 1)
			id = (int) v;
		else if (m == 2) {
			if (extType == 1 && img != v)
				for (int i = 0; i < randSeries.size(); i++) {
					int r = randSeries.get(i);

					r += 1;
					r = r > 3 ? 0 : r;

					randSeries.set(i, r);
				}

			img = (int) v;
		} else if (m == 3)
			z = (int) (v * b.ent.length + ind);
		else if (m == 4)
			pos.x = args[4] + v;
		else if (m == 5)
			pos.y = args[5] + v;
		else if (m == 6)
			piv.x = (args[6] + v) * (b.pvt != b.flipped && fa == null ? -1 : 1);
		else if (m == 7)
			piv.y = args[7] + v;
		else if (m == 8) {
			sca.x = 1f * args[8] * v / b.model.ints[0] * (b.flipped && fa == null ? -1 : 1);
			sca.y = 1f * args[9] * v / b.model.ints[0];
		} else if (m == 53)
			gsca = v;
		else if (m == 9)
			sca.x = 1f * args[8] * v / b.model.ints[0] * (b.flipped && fa == null ? -1 : 1);
		else if (m == 10)
			sca.y = 1f * args[9] * v / b.model.ints[0];
		else if (m == 11)
			angle = (args[10] + v) * (b.flipped ? -1 : 1);
		else if (m == 12)
			opacity = v * args[11] / b.model.ints[2];
		else if (m == 13)
			hf = v == 0 ? 1 : -1;
		else if (m == 14)
			vf = v == 0 ? 1 : -1;
		else if (m == 50) {
			extendX = v;
			extType = 0;
		} else if (m == 51) {
			extendX = v;
			extType = 1;
		} else if (m == 52) {
			extendY = v;
			extType = 0;
		} else
			CommonStatic.ctx.printErr(ErrType.NEW, "unhandled modification " + m);

	}

	@Override
	public int compareTo(EPart o) {
		return Float.compare(z, o.z);
	}

	public float getVal(int m) {
		if (m == 0)
			return getPar();
		else if (m == 1)
			return id;
		else if (m == 2)
			return img;
		else if (m == 3)
			return (float)(z - ind) / b.ent.length;
		else if (m == 4)
			return (int) pos.x;
		else if (m == 5)
			return (int) pos.y;
		else if (m == 6)
			return (int) piv.x;
		else if (m == 7)
			return (int) piv.y;
		else if (m == 8)
			return gsca;
		else if (m == 9)
			return (int) sca.x;
		else if (m == 10)
			return (int) sca.y;
		else if (m == 11)
			return angle;
		else if (m == 12)
			return opacity;
		else if (m == 13)
			return hf;
		else if (m == 14)
			return vf;
		else if (m == 50)
			return extendX;
		else if (m == 51)
			return extendX;
		else if (m == 52)
			return extendY;
		else if (m == 53)
			return gsca;
		else
			System.out.println("EPart modification can be: " + m);
		return -1;
	}

	@Override
	public String toString() {
		return name;
	}

	public MaModel getModel() {
		return b.model;
	}

	public P getSca() {
		return sca;
	}

	public EPart[] getParts() {
		return b.ent;
	}

	public int getInd() {
		return ind;
	}

	public void drawPart(FakeGraphics g, P base) {
		if (img < 0 || id < 0 || opa() < CommonStatic.getConfig().deadOpa * 0.01 + 1e-5 || b.a.parts(img) == null)
			return;
		FakeTransform at = g.getTransform();
		transform(g, base);
		FakeImage bimg = b.a.parts(img);
		int w = bimg.getWidth();
		int h = bimg.getHeight();
		P p0 = getSize();
		P tpiv = P.newP(piv).times(p0).times(base);
		P sc = P.newP(w, h).times(p0).times(base);
		P.delete(p0);
		if (extType == 0)
			drawImg(g, bimg, tpiv, sc, opa(), glow, extendX / b.model.ints[0], extendY / b.model.ints[0]);
		else if (extType == 1)
			drawRandom(g, new FakeImage[] { b.a.parts(3), b.a.parts(4), b.a.parts(5), b.a.parts(6) }, tpiv, sc, opa(),
					glow == 1, extendX / b.model.ints[0], extendY / b.model.ints[0]);
		P.delete(tpiv);
		P.delete(sc);
		g.setTransform(at);
		g.delete(at);
	}

	/**
	 * Draw part with specified opacity (Os). If part already has its own opacity (Oo), then formula is Oo * Op
	 * @param g Graphic
	 * @param base Base
	 * @param opacity Opacity, range is 0 ~ 255
	 */
	protected void drawPartWithOpacity(FakeGraphics g, P base, int opacity) {
		if (img < 0 || id < 0 || opa() < CommonStatic.getConfig().deadOpa * 0.01 + 1e-5 || b.a.parts(img) == null)
			return;
		FakeTransform at = g.getTransform();
		transform(g, base);
		FakeImage bimg = b.a.parts(img);
		int w = bimg.getWidth();
		int h = bimg.getHeight();
		P p0 = getSize();
		P tpiv = P.newP(piv).times(p0).times(base);
		P sc = P.newP(w, h).times(p0).times(base);
		P.delete(p0);
		if (extType == 0)
			drawImg(g, bimg, tpiv, sc, opa() * opacity / 255f, glow, extendX / b.model.ints[0], extendY / b.model.ints[0]);
		else if (extType == 1)
			drawRandom(g, new FakeImage[] { b.a.parts(3), b.a.parts(4), b.a.parts(5), b.a.parts(6) }, tpiv, sc, opa(),
					glow == 1, extendX / b.model.ints[0], extendY / b.model.ints[0]);
		P.delete(tpiv);
		P.delete(sc);
		g.setTransform(at);
		g.delete(at);
	}

	protected void drawScale(FakeGraphics g, P base) {
		FakeImage bimg = b.a.parts(img);
		if (bimg == null)
			return;
		FakeTransform at = g.getTransform();
		transform(g, base);
		int w = bimg.getWidth();
		int h = bimg.getHeight();
		P p0 = getSize();
		P tpiv = P.newP(piv).times(p0).times(base);
		P sc = P.newP(w, h).times(p0).times(base);
		P.delete(p0);
		drawSca(g, tpiv, sc);
		P.delete(tpiv);
		P.delete(sc);
		g.setTransform(at);
		g.delete(at);
	}

	protected void setPara(EPart p) {
		if (p == null) {
			fa = para;
			para = null;
		} else {
			para = fa;
			fa = p;
		}
	}

	protected void setValue() {
		if (args[0] >= b.ent.length)
			args[0] = 0;
		fa = args[0] <= -1 ? null : b.ent[args[0]];
		id = args[1];
		img = args[2];
		z = args[3] * b.ent.length + ind;
		pos = pos.setTo(args[4], args[5]);
		piv = piv.setTo(args[6] * (b.pvt != b.flipped && fa == null ? -1 : 1), args[7]);
		sca = sca.setTo(args[8] * (b.flipped && fa == null ? -1 : 1), args[9]);
		angle = args[10] * (b.flipped ? -1 : 1);
		opacity = args[11];
		glow = args[12];
		gsca = b.model.ints[0];
		hf = vf = 1;
		extendX = extendY = 0;
	}

	public P getSize() {
		float mi = 1f / b.model.ints[0];
		if (fa == null)
			return P.newP(sca).times(gsca * mi * mi);
		return fa.getSize().times(sca).times(gsca * mi * mi);
	}

	private P getBaseSize(boolean parent) {
		if(b.model.confs.length > 0) {
			float mi = 1f / b.model.ints[0];

			if(parent) {
				if(fa != null) {
					return fa.getBaseSize(true).times(Math.signum(b.model.parts[ind][8]), Math.signum(b.model.parts[ind][9]));
				} else {
					return P.newP(Math.signum(b.model.parts[ind][8]), Math.signum(b.model.parts[ind][9]));
				}
			} else {
				if(b.model.confs[0][0] == -1) {
					return P.newP(b.model.parts[0][8] * mi, b.model.parts[0][9] * mi);
				} else {
					if (b.model.confs[0][0] == ind) {
						return P.newP(b.model.parts[b.model.confs[0][0]][8] * mi, b.model.parts[b.model.confs[0][0]][9] * mi);
					} else {
						return b.ent[b.model.confs[0][0]].getBaseSize(true).times(b.model.parts[b.model.confs[0][0]][8] * mi, b.model.parts[b.model.confs[0][0]][9] * mi);
					}
				}
			}
		} else {
			return P.newP(1f, 1f);
		}
	}

	public float opa() {
		if (opacity == 0)
			return 0;
		if (fa != null)
			return fa.opa() * opacity / b.model.ints[2];
		return opacity / b.model.ints[2];
	}

	private void transform(FakeGraphics g, P sizer) {
		P siz = sizer;
		if (fa != null) {
			fa.transform(g, sizer);
			siz = fa.getSize().times(sizer);
		}

		if (b.ent[0] != this) {
			P tpos = P.newP(pos).times(siz);
			g.translate(tpos.x, tpos.y);
			g.scale(hf, vf);
			P.delete(tpos);
		} else if (fa != null && b.model.ints[2] == 255 && b.EWarp) {
			int[] fir = fa.b.model.parts[0];
			int[] data = b.model.confs[0];
			//Ignore these warnings
			P tpos = P.newP(data[2] * b.model.parts[0][8] / b.model.ints[0] - fir[6], data[3] * b.model.parts[0][8] / b.model.ints[0] - fir[7]).times(siz);
			g.translate(-tpos.x, -tpos.y);
			tpos = P.newP(-piv.x, piv.y).times(siz);
			g.translate(tpos.x, tpos.y);

			g.scale(hf, vf);
			P.delete(tpos);
		} else {
			if (b.model.confs.length > 0) {
				int[] data = b.model.confs[0];
				P p0 = getBaseSize(false);

				P shi = P.newP(data[2] * (b.pvt ? -1 : 1), data[3]).times(p0);
				P.delete(p0);
				P p3 = shi.times(sizer);
				g.translate(-p3.x, -p3.y);

				P.delete(shi);
			}
			P p0 = getSize();
			P p = P.newP(piv).times(p0).times(sizer);
			P.delete(p0);
			g.translate(p.x, p.y);

			P.delete(p);
		}
		if (angle != 0)
			g.rotate((float) (Math.PI * 2 * angle / b.model.ints[1]));

		if (fa != null)
			P.delete(siz);
	}
}