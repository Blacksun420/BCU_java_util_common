package common.util.pack.bgeffect;

import common.CommonStatic;
import common.system.P;
import common.system.fake.FakeGraphics;
import common.util.Data;
import common.util.pack.Background;

public class SnowStarBGEffect extends BackgroundEffect {
    @Override
    public void check() {
        CommonStatic.getBCAssets().bgEffects.get(Data.BG_EFFECT_STAR).check();
        CommonStatic.getBCAssets().bgEffects.get(Data.BG_EFFECT_SNOW).check();
    }

    @Override
    public void preDraw(FakeGraphics g, P rect, double siz, double midH) {
        CommonStatic.getBCAssets().bgEffects.get(Data.BG_EFFECT_STAR).preDraw(g, rect, siz, midH);
    }

    @Override
    public void postDraw(FakeGraphics g, P rect, double siz, double midH) {
        CommonStatic.getBCAssets().bgEffects.get(Data.BG_EFFECT_SNOW).postDraw(g, rect, siz, midH);
    }

    @Override
    public void draw(FakeGraphics g, double x, double y, double siz, int groundH, int skyH) {
        CommonStatic.getBCAssets().bgEffects.get(Data.BG_EFFECT_STAR).draw(g, x, y, siz, groundH, skyH);
        CommonStatic.getBCAssets().bgEffects.get(Data.BG_EFFECT_SNOW).draw(g, x, y, siz, groundH, skyH);
    }

    @Override
    public void update(int w, double h, double midH) {
        CommonStatic.getBCAssets().bgEffects.get(Data.BG_EFFECT_STAR).update(w, h, midH);
        CommonStatic.getBCAssets().bgEffects.get(Data.BG_EFFECT_SNOW).update(w, h, midH);
    }

    @Override
    public void initialize(int w, double h, double midH, Background bg) {
        CommonStatic.getBCAssets().bgEffects.get(Data.BG_EFFECT_STAR).initialize(w, h, midH, bg);
        CommonStatic.getBCAssets().bgEffects.get(Data.BG_EFFECT_SNOW).initialize(w, h, midH, bg);
    }
}
