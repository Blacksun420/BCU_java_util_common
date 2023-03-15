package common.util.pack.bgeffect;

import com.google.gson.JsonElement;
import common.CommonStatic;
import common.io.json.JsonClass;
import common.io.json.JsonDecoder;
import common.io.json.JsonField;
import common.pack.*;
import common.system.P;
import common.system.fake.FakeGraphics;
import common.system.fake.FakeImage;
import common.system.files.VFile;
import common.util.Data;
import common.util.anim.ImgCut;
import common.util.pack.Background;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@IndexContainer.IndexCont(PackData.class)
@JsonClass.JCGeneric(Identifier.class)
@JsonClass(read = JsonClass.RType.MANUAL, generator = "construct")
public abstract class BackgroundEffect extends Data implements IndexContainer.Indexable<PackData, BackgroundEffect> {
    //Only MixedBGEffect and CustomBGEffect need Custom dependence. The rest only matter for defPack.
    public static final int BGHeight = 512;
    public static final int battleOffset = (int) (400 / CommonStatic.BattleConst.ratio);
    public static final List<Integer> jsonList = new ArrayList<>();

    /**
     * Used for manual constructor. Do not delete
     * @param elem Json Data
     * @return Object decoded to its proper class
     */
    @SuppressWarnings("unused")
    public static Object construct(JsonElement elem) {
        if (elem.getAsJsonObject().has("spacer"))
            return JsonDecoder.decode(elem, CustomBGEffect.class);
        return JsonDecoder.decode(elem, MixedBGEffect.class);
    }

    public static void read() {
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
                assets.bgEffects.get(Data.BG_EFFECT_STAR), assets.bgEffects.get(Data.BG_EFFECT_SNOW)));

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
                JsonBGEffect jbg = new JsonBGEffect(Identifier.rawParseInt(id, BackgroundEffect.class), false);
                assets.bgEffects.add(jbg);
                assets.bgs.getRaw(id).bgEffect = jbg.getID();
            }

            for (int j = 0; j < assets.bgEffects.size(); j++) {
                BackgroundEffect a = assets.bgEffects.get(j);

                if(a instanceof JsonBGEffect && ((JsonBGEffect)a).postNeed) {
                    try {
                        assets.bgEffects.set(j, new JsonBGEffect(a.id, true));
                    } catch (IOException ignored) {
                    }
                }
            }

            for(int i = 0; i < assets.bgs.size(); i++) {
                Background bg = assets.bgs.get(i);
                if(bg.reference != null) {
                    Background ref = bg.reference.get();

                    if(ref == null)
                        continue;

                    if(bg.bgEffect == null)
                        bg.bgEffect = ref.bgEffect;
                    else if(ref.bgEffect != null)
                        if (bg.bgEffect.cls == MixedBGEffect.class)
                            ((MixedBGEffect) bg.bgEffect.get()).effects.add(ref.bgEffect.get());
                        else {
                            MixedBGEffect temp = new MixedBGEffect(Identifier.rawParseInt(bg.id.id, BackgroundEffect.class), bg.bgEffect.get(), ref.bgEffect.get());
                            assets.bgEffects.add(temp);
                            bg.bgEffect = temp.getID();
                        }
                }
            }
            //Handle BG 197 (Commented because 12.2 assets don't exist yet)
            Background zbg = UserProfile.getBCData().bgs.getRaw(197);
            MixedBGEffect zeff = new MixedBGEffect(Identifier.rawParseInt(zbg.id.id, BackgroundEffect.class), zbg.bgEffect.get(), assets.bgEffects.get(Data.BG_EFFECT_SNOW));
            assets.bgEffects.add(zeff);
            zbg.bgEffect = zeff.getID();
        }, Context.ErrType.FATAL, "Failed to read bg effect data");
    }

    @JsonClass.JCIdentifier
    @JsonField
    public final Identifier<BackgroundEffect> id;
    protected BackgroundEffect(Identifier<BackgroundEffect> id) {
        this.id = id;
    }

    /**
     * Load image or any data here
     */
    public abstract void check();

    /**
     * Effects which will be drawn behind entities
     * @param g Canvas
     * @param rect (x,y) coordinate of battle
     * @param siz size of battle
     * @param midH how battle will be shifted along y-axis
     */
    public abstract void preDraw(FakeGraphics g, P rect, final double siz, final double midH);

    /**
     * Effects which will be drawn in front of entities
     * @param g Canvas
     * @param rect (x,y) coordinate of battle
     * @param siz size of battle
     * @param midH how battle will be shifted along y-axis
     */
    public abstract void postDraw(FakeGraphics g, P rect, final double siz, final double midH);

    /**
     * Used for Background preview only. Draws everything at once with no battle scaling
     */
    public abstract void draw(FakeGraphics g, double x, double y, double siz, int groundH, int skyH);

    /**
     * Update data here
     * @param w Width of battlefield as P
     * @param h Height of battlefield as Px
     */
    public abstract void update(int w, double h, double midH);

    /**
     * Initialize data here
     * @param w Width of battlefield as P
     * @param h Height of battlefield as Px
     */
    public abstract void initialize(int w, double h, double midH, Background bg);

    public void release() {

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
    public Identifier<BackgroundEffect> getID() {
        return id;
    }

    public String getName() {
        return "";
    }
}
