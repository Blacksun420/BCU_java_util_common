package common.pack.oldFix;

import common.pack.Source;
import common.system.VImg;
import common.system.fake.FakeImage;
import common.system.fake.ImageBuilder;
import common.system.files.FDByte;
import common.util.anim.AnimU;
import common.util.anim.ImgCut;
import common.util.anim.MaAnim;
import common.util.anim.MaModel;

public class PCAL extends Source.SourceAnimLoader {

    private final FakeImage num;
    private final ImgCut imgcut;
    private final MaModel mamodel;
    private final MaAnim[] anims;
    private VImg uni, edi;
    private static final int[] BCUPACK_VER = {401, 0};

    PCAL(String target, ISStream is) {
        super(new Source.ResourceLocation(target, "local animation", Source.BasePath.ANIM), null);
        num = FakeImage.read(is.nextBytesI());
        imgcut = ImgCut.newIns(new FDByte(is.nextBytesI()));
        mamodel = MaModel.newIns(new FDByte(is.nextBytesI()));
        int n = is.nextInt();
        anims = new MaAnim[n];
        for (int i = 0; i < n; i++)
            anims[i] = MaAnim.newIns(new FDByte(is.nextBytesI()), BCUPACK_VER);
        if (!is.end()) {
            VImg vimg = ImageBuilder.toVImg(is.nextBytesI());
            if (vimg.getImg().getHeight() == 32)
                edi = vimg;
            else
                uni = vimg;
        }
        if (!is.end())
            uni = ImageBuilder.toVImg(is.nextBytesI());
    }

    PCAL(String target, ISStream is, VerFixer.ImgReader r) {
        super(new Source.ResourceLocation(target, "local animation", Source.BasePath.ANIM), null);
        is.nextString();
        num = r.readImg(is.nextString());
        edi = r.readImgOptional(is.nextString());
        uni = r.readImgOptional(is.nextString());
        imgcut = ImgCut.newIns(new FDByte(is.nextBytesI()));
        mamodel = MaModel.newIns(new FDByte(is.nextBytesI()));
        int n = is.nextInt();
        anims = new MaAnim[n];
        for (int i = 0; i < n; i++)
            anims[i] = MaAnim.newIns(new FDByte(is.nextBytesI()), BCUPACK_VER);
    }

    @Override
    public VImg getIcon(String path) {
        if (path.equals(EDI))
            return edi;
        if (path.equals(UNI))
            return uni;
        return null;
    }

    @Override
    public ImgCut getIC() {
        return imgcut;
    }

    @Override
    public MaAnim[] getMA() {
        return anims;
    }

    @Override
    public MaModel getMM() {
        return mamodel;
    }

    @Override
    public FakeImage getNum() {
        return num;
    }

    public int getStatus() {
        return 1;
    }
}