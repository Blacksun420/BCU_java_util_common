package common.util.anim;

import common.CommonStatic;
import common.pack.Source.BasePath;
import common.system.VImg;
import common.system.fake.FakeImage;
import common.system.fake.FakeImage.Marker;
import common.system.files.FileData;
import common.system.files.VFile;

public class AnimUD extends AnimU<AnimUD.DefImgLoader> {

	private final String name;

	static class DefImgLoader implements AnimU.ImageKeeper {

		private final String spath;
		private final VFile fnum, fedi, funi;
		private FileData dnum;
		private FakeImage num;
		private VImg edi, uni;

		private DefImgLoader(String path, String str, String sedi, String suni) {
			spath = path + str;
			fnum = VFile.get(spath + ".png");
			fedi = sedi == null ? null : VFile.get(path + sedi);
			funi = suni == null ? null : VFile.get(path + suni);
		}

		@Override
		public VImg getEdi() {
			if (edi != null)
				return edi;
			return fedi == null ? null : (edi = new VImg(fedi).mark(Marker.EDI));
		}

		@Override
		public ImgCut getIC() {
			return ImgCut.newIns(spath + ".imgcut");
		}

		@Override
		public MaAnim[] getMA() {
			MaAnim[] ma;
			if (VFile.get(spath + ".maanim") != null) {
				ma = new MaAnim[] { MaAnim.newIns(spath + ".maanim") };
			} else {
				if (VFile.get(spath + "_zombie00.maanim") != null)
					ma = new MaAnim[7];
				else if (VFile.get(spath + "_entry.maanim") != null)
					ma = new MaAnim[5];
				else
					ma = new MaAnim[4];
				for (int i = 0; i < 4; i++)
					ma[i] = MaAnim.newIns(spath + "0" + i + ".maanim");
				if (ma.length == 5)
					ma[4] = MaAnim.newIns(spath + "_entry.maanim");
				if (ma.length == 7)
					for (int i = 0; i < 3; i++)
						ma[i + 4] = MaAnim.newIns(spath + "_zombie0" + i + ".maanim");
			}
			ma = filterValidAnims(ma);
			return ma;
		}

		@Override
		public MaModel getMM() {
			return MaModel.newIns(spath + ".mamodel");
		}

		@Override
		public BasePath getBasePath() {
			if (getMA().length == 1)
				return BasePath.SOUL;
			return BasePath.ANIM;
		}

		@Override
		public FakeImage getNum() {
			if (num != null)
				return num;
			FileData fd = dnum == null ? (dnum = fnum.getData()) : dnum;
			num = fd.getImg();
			return num;
		}

		@Override
		public VImg getUni() {
			if (uni != null)
				return uni;
			return funi == null ? CommonStatic.getBCAssets().slot[0] : (uni = new VImg(funi).mark(Marker.UNI));
		}

		@Override
		public void unload() {
			dnum = null;

			if (num != null) {
				num.unload();
				num = null;
			}
		}

		private MaAnim[] filterValidAnims(MaAnim[] original) {
			int end = 0;

			for (int i = 0; i < original.length; i++) {
				if (original[i] != null && original[i].n != 0)
					end = i;
			}

			MaAnim[] fixed = new MaAnim[end + 1];

			System.arraycopy(original, 0, fixed, 0, end + 1);

			return fixed;
		}
	}

	public AnimUD(String path, String name, String edi, String uni) {
		super(path + name, new DefImgLoader(path, name, edi, uni));
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
