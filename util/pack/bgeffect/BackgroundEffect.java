package common.util.pack.bgeffect;

import com.google.gson.JsonElement;
import common.CommonStatic;
import common.io.json.JsonClass;
import common.io.json.JsonDecoder;
import common.pack.*;
import common.system.P;
import common.system.fake.FakeGraphics;
import common.system.fake.FakeImage;
import common.system.files.VFile;
import common.util.Data;
import common.util.anim.ImgCut;
import common.util.pack.Background;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@IndexContainer.IndexCont(PackData.class)
@JsonClass.JCGeneric(BackgroundEffect.BGIdentifier.class)
@JsonClass(read = JsonClass.RType.MANUAL, generator = "construct")
public interface BackgroundEffect extends IndexContainer.Indexable<PackData, BackgroundEffect> {
    //Only MixedBGEffect and CustomBGEffect need Custom dependence. The rest only matter for defPack.
    int BGHeight = 512;
    int battleOffset = (int) (400 / CommonStatic.BattleConst.ratio);
    List<Integer> jsonList = new ArrayList<>();

    @JsonClass(noTag = JsonClass.NoTag.LOAD)
    class BGIdentifier {
        public Identifier<BackgroundEffect> id;

        @JsonClass.JCConstructor
        public BGIdentifier() {
            id = null;
        }

        @JsonClass.JCConstructor
        public BGIdentifier(BackgroundEffect bge) {
            id = bge.getID();
        }

        @JsonClass.JCGetter
        public BackgroundEffect getter() {
            return id.get();
        }
    }

    /**
     * Used for manual constructor. Do not delete
     * @param elem Json Data
     * @return Object decoded to its proper class
     */
    static Object construct(JsonElement elem) {
        if (elem.getAsJsonObject().has("spacer"))
            return JsonDecoder.decode(elem, CustomBGEffect.class);
        return JsonDecoder.decode(elem, MixedBGEffect.class);
    }

    static void read() {
        PackData.DefPack assets = UserProfile.getBCData();

        assets.bgEffects.add(new StarBackgroundEffect(assets.getNextID(BackgroundEffect.class)));

        VFile rainFile = VFile.get("./org/battle/a/000_a.png");

        FakeImage rainImage = rainFile.getData().getImg();
        ImgCut rainCut = ImgCut.newIns("./org/battle/a/000_a.imgcut");

        FakeImage[] images = rainCut.cut(rainImage);

        assets.bgEffects.add(new RainBGEffect(assets.getNextID(BackgroundEffect.class), images[29], images[28]));

        VFile bubbleFile = VFile.get("./org/img/bgEffect/bubble02.png");

        FakeImage bubbleImage = bubbleFile.getData().getImg();

        assets.bgEffects.add(new BubbleBGEffect(assets.getNextID(BackgroundEffect.class), bubbleImage));

        VFile secondBubbleFile = VFile.get("./org/img/bgEffect/bubble03_bg040.png");

        FakeImage secondBubbleImage = secondBubbleFile.getData().getImg();

        assets.bgEffects.add(new FallingSnowBGEffect(assets.getNextID(BackgroundEffect.class), secondBubbleImage));

        VFile snowFile = VFile.get("./org/img/bgEffect/img021.png");

        FakeImage snowImage = snowFile.getData().getImg();

        assets.bgEffects.add(new SnowBGEffect(assets.getNextID(BackgroundEffect.class), snowImage));

        assets.bgEffects.add(new MixedBGEffect(assets.getNextID(BackgroundEffect.class),
                UserProfile.getBCData().bgEffects.get(Data.BG_EFFECT_STAR), UserProfile.getBCData().bgEffects.get(Data.BG_EFFECT_SNOW)));

        assets.bgEffects.add(new BlizzardBGEffect(assets.getNextID(BackgroundEffect.class), secondBubbleImage));

        assets.bgEffects.add(new ShiningBGEffect(assets.getNextID(BackgroundEffect.class)));

        assets.bgEffects.add(new BalloonBGEffect(assets.getNextID(BackgroundEffect.class)));

        assets.bgEffects.add(new RockBGEffect(assets.getNextID(BackgroundEffect.class)));

        CommonStatic.ctx.noticeErr(() -> {
            VFile vf = VFile.get("./org/data/");

            if(vf != null) {
                Collection<VFile> fileList = vf.list();

                if(fileList != null) {
                    for(VFile file : fileList) {
                        if(file == null)
                            continue;

                        if(file.name.matches("bg\\d+\\.json") && file.getData().size() != 0) {
                            jsonList.add(CommonStatic.parseIntN(file.name));
                        }
                    }
                }
            }

            jsonList.sort(Integer::compareTo);
            for (Integer id : jsonList) {
                JsonBGEffect jbg = new JsonBGEffect(assets.getNextID(BackgroundEffect.class), id);
                assets.bgEffects.add(jbg);
                UserProfile.getBCData().bgs.getRaw(id).bgEffect = jbg.getID();
            }

            for(int i = 0; i < UserProfile.getBCData().bgs.size(); i++) {
                Background bg = UserProfile.getBCData().bgs.get(i);

                if(bg.reference != -1) {
                    Background ref = assets.bgs.findByID(bg.reference);

                    if(ref == null)
                        continue;

                    if(bg.bgEffect == null)
                        bg.bgEffect = ref.bgEffect;
                    else if(ref.bgEffect != null) {
                        if (bg.bgEffect.cls == MixedBGEffect.class)
                            ((MixedBGEffect) bg.bgEffect.get()).effects.add(ref.bgEffect.get());
                        else
                            bg.bgEffect = new MixedBGEffect(assets.getNextID(BackgroundEffect.class), bg.bgEffect.get(), ref.bgEffect.get()).getID();
                    }
                }
            }
            for(int i = 0; i < UserProfile.getBCData().bgs.size(); i++) {
                Background bg = UserProfile.getBCData().bgs.get(i);
                if (bg.bgEffect != null && bg.bgEffect.cls == MixedBGEffect.class)
                    assets.bgEffects.add(bg.bgEffect.get());
            }
        }, Context.ErrType.FATAL, "Failed to read bg effect data");
    }

    /**
     * Load image or any data here
     */
    void check();

    /**
     * Effects which will be drawn behind entities
     * @param g Canvas
     * @param rect (x,y) coordinate of battle
     * @param siz size of battle
     * @param midH how battle will be shifted along y-axis
     */
    void preDraw(FakeGraphics g, P rect, final double siz, final double midH);

    /**
     * Effects which will be drawn in front of entities
     * @param g Canvas
     * @param rect (x,y) coordinate of battle
     * @param siz size of battle
     * @param midH how battle will be shifted along y-axis
     */
    void postDraw(FakeGraphics g, P rect, final double siz, final double midH);

    /**
     * Used for Background preview only. Draws everything at once with no battle scaling
     */
    void draw(FakeGraphics g, double x, double y, double siz, int groundH, int skyH);

    /**
     * Update data here
     * @param w Width of battlefield as P
     * @param h Height of battlefield as Px
     */
    void update(int w, double h, double midH);

    /**
     * Initialize data here
     * @param w Width of battlefield as P
     * @param h Height of battlefield as Px
     */
    void initialize(int w, double h, double midH, Background bg);

    default void release() {

    }

    /**
     * Convert battle unit to pixel unit
     * @param p Position in battle
     * @param siz Size of battle
     * @return Converted pixel
     */
    static int convertP(double p, double siz) {
        return (int) (p * CommonStatic.BattleConst.ratio * siz);
    }

    static int revertP(double px) {
        return (int) (px / CommonStatic.BattleConst.ratio);
    }

    @Override
    Identifier<BackgroundEffect> getID();

    default String getName() {
        return "";
    }
}
