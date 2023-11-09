package common.util.pack.bgeffect;

import common.CommonStatic;
import common.io.json.JsonClass;
import common.pack.Context;
import common.pack.Identifier;
import common.pack.UserProfile;
import common.system.P;
import common.system.VImg;
import common.system.fake.FakeGraphics;
import common.system.files.VFile;
import common.util.Data;
import common.util.anim.ImgCut;
import common.util.pack.Background;

import java.io.IOException;
import java.util.*;

@JsonClass.JCGeneric(Identifier.class)
@JsonClass
public abstract class BackgroundEffect {
    public static Map<Integer, MixedBGEffect> mixture = new HashMap<>();
    public static int BGHeight = 512;
    public static final int battleOffset = (int) (400 / CommonStatic.BattleConst.ratio);
    public static final List<Integer> jsonList = new ArrayList<>();

    public static void read() {
        CommonStatic.BCAuxAssets asset = CommonStatic.getBCAssets();

        asset.bgEffects.add(new StarBackgroundEffect());

        asset.bgEffects.add(new RainBGEffect(new VImg("./org/battle/a/000_a.png"), ImgCut.newIns("./org/battle/a/000_a.imgcut")));

        asset.bgEffects.add(new BubbleBGEffect(new VImg("./org/img/bgEffect/bubble02.png")));

        asset.bgEffects.add(new FallingSnowBGEffect(new VImg("./org/img/bgEffect/bubble03_bg040.png")));

        asset.bgEffects.add(new SnowBGEffect(new VImg("./org/img/bgEffect/img021.png")));

        asset.bgEffects.add(new SnowStarBGEffect());

        asset.bgEffects.add(new BlizzardBGEffect(new VImg("./org/img/bgEffect/bubble03_bg040.png")));

        asset.bgEffects.add(new ShiningBGEffect());

        asset.bgEffects.add(new BalloonBGEffect());

        asset.bgEffects.add(new RockBGEffect());

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

            int currentSize = asset.bgEffects.size();

            for (Integer id : jsonList) {
                asset.bgEffects.add(new JsonBGEffect(id, false));

                UserProfile.getBCData().bgs.getRaw(id).effect = currentSize;

                currentSize++;
            }

            asset.bgEffects.replaceAll(a -> {
                if(!(a instanceof JsonBGEffect) || !((JsonBGEffect) a).postNeed)
                    return a;

                try {
                    return new JsonBGEffect(((JsonBGEffect) a).id, true);
                } catch (IOException ignored) {
                    return a;
                }
            });

            for(int i = 0; i < UserProfile.getBCData().bgs.size(); i++) {
                Background bg = UserProfile.getBCData().bgs.get(i);

                if(bg.reference != -1) {
                    Background ref = UserProfile.getBCData().bgs.findByID(bg.reference);

                    if(ref == null)
                        continue;

                    if(bg.effect == -1)
                        bg.effect = ref.effect;
                    else if(bg.effect >= 0 && ref.effect != -1) {
                        if(ref.effect >= 0) {
                            mixture.put(bg.id.id, new MixedBGEffect(asset.bgEffects.get(bg.effect), asset.bgEffects.get(ref.effect)));

                            bg.effect = -bg.id.id;
                        } else if(ref.effect == -ref.id.id && mixture.containsKey(ref.id.id)) {
                            mixture.put(bg.id.id, new MixedBGEffect(asset.bgEffects.get(bg.effect), mixture.get(ref.id.id)));

                            bg.effect = -bg.id.id;
                        } else if(ref.id.id != -ref.id.id || !mixture.containsKey(ref.id.id)) {
                            System.out.println("W/BackgroundEffect::read - Unhandled situation for background effect mixing -> Reference BG ID : "+ref.id.id+" | Mixture contains key : "+mixture.containsKey(ref.id.id));
                        }
                    }
                } else if(bg.id.id == 197) {
                    mixture.put(bg.id.id, new MixedBGEffect(asset.bgEffects.get(bg.effect), asset.bgEffects.get(Data.BG_EFFECT_SNOW)));

                    bg.effect = -bg.id.id;
                }
            }
        }, Context.ErrType.FATAL, "Failed to read bg effect data");
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
    public abstract void preDraw(FakeGraphics g, P rect, final float siz, final float midH);

    /**
     * Effects which will be drawn in front of entities
     * @param g Canvas
     * @param rect (x,y) coordinate of battle
     * @param siz size of battle
     * @param midH how battle will be shifted along y-axis
     */
    public abstract void postDraw(FakeGraphics g, P rect, final float siz, final float midH);

    /**
     * Update data here
     * @param w Width of battlefield as P
     * @param h Height of battlefield as Px
     */
    public abstract void update(int w, float h, float midH);

    public void updateAnimation(int w, float h, float midH) {
        update(w, h, midH);
    }

    /**
     * Initialize data here
     * @param w Width of battlefield as P
     * @param h Height of battlefield as Px
     */
    public abstract void initialize(int w, float h, float midH, Background bg);

    public void release() {

    }

    /**
     * Convert battle unit to pixel unit
     * @param p Position in battle
     * @param siz Size of battle
     * @return Converted pixel
     */
    protected int convertP(float p, float siz) {
        return (int) (p * CommonStatic.BattleConst.ratio * siz);
    }

    protected int revertP(float px) {
        return (int) (px / CommonStatic.BattleConst.ratio);
    }
}
