package common.util.pack.bgeffect;

import common.CommonStatic;
import common.io.json.JsonClass;
import common.pack.Identifier;
import common.system.P;
import common.system.fake.FakeGraphics;
import common.system.fake.FakeImage;
import common.util.Data;
import common.util.pack.Background;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

@JsonClass.JCGeneric(Identifier.class)
public class BubbleBGEffect extends BackgroundEffect {
    private final FakeImage bubble;

    private final int bw;
    private final int bh;

    private final List<P> bubblePosition = new ArrayList<>();
    private final List<Byte> differentiator = new ArrayList<>();
    private final Random r = new Random();

    private final List<Integer> capture = new LinkedList<>();

    public BubbleBGEffect(Identifier<BackgroundEffect> i, FakeImage bubble) {
        super(i);
        this.bubble = bubble;

        bw = (int) (this.bubble.getWidth() * 1.8);
        bh = (int) (this.bubble.getHeight() * 1.8);
    }

    @Override
    public void check() {
        bubble.bimg();
    }

    @Override
    public void preDraw(FakeGraphics g, P rect, float siz, float midH) {

    }

    @Override
    public void postDraw(FakeGraphics g, P rect, float siz, float midH) {
        for(int i = 0; i < bubblePosition.size(); i++) {
            g.drawImage(
                    bubble,
                    BackgroundEffect.convertP((float) (bubblePosition.get(i).x + Data.BG_EFFECT_BUBBLE_FACTOR * Math.sin(differentiator.get(i) + bubblePosition.get(i).y / Data.BG_EFFECT_BUBBLE_STABILIZER)), siz) + (int) rect.x,
                    (int) (bubblePosition.get(i).y * siz - rect.y + midH * siz),
                    bw * siz, bh * siz
            );
        }
    }

    @Override
    public void draw(FakeGraphics g, float y, float siz, float midH) {
        for(int i = 0; i < bubblePosition.size(); i++) {
            g.drawImage(
                    bubble,
                    BackgroundEffect.convertP((float) (bubblePosition.get(i).x + Data.BG_EFFECT_BUBBLE_FACTOR * Math.sin(differentiator.get(i) + bubblePosition.get(i).y / Data.BG_EFFECT_BUBBLE_STABILIZER)), siz),
                    (int) (bubblePosition.get(i).y * siz - y + midH * siz),
                    bw * siz, bh * siz
            );
        }
    }

    @Override
    public void update(int w, float h, float midH) {
        capture.clear();

        for(int i = 0; i < bubblePosition.size(); i++) {
            if(bubblePosition.get(i).y < -bh)
                capture.add(i);
            else if (CommonStatic.getConfig().fps60)
                bubblePosition.get(i).y -= (BGHeight * 3.0 + bh) / Data.BG_EFFECT_BUBBLE_TIME / 2.0;
            else {
                bubblePosition.get(i).y -= (BGHeight * 3.0 + bh) / Data.BG_EFFECT_BUBBLE_TIME;
            }
        }

        if(!capture.isEmpty())
            for (Integer capt : capture) {
                P.delete(bubblePosition.get(capt));

                bubblePosition.set(capt, P.newP(r.nextInt(w + battleOffset), BGHeight * 3));
                differentiator.set(capt, (byte) (3 - r.nextInt(6)));
            }
    }

    @Override
    public void initialize(int w, float h, float midH, Background bg) {
        for (P p : bubblePosition)
            P.delete(p);

        bubblePosition.clear();
        differentiator.clear();

        int number = w / 200 - (r.nextInt(w) / 1000);

        for(int i = 0; i < number; i++) {
            bubblePosition.add(P.newP(r.nextInt(w + battleOffset), r.nextFloat() * (BGHeight * 3f + bh)));
            differentiator.add((byte) (3 - r.nextInt(6)));
        }
    }

    @Override
    public String toString() {
        return CommonStatic.def.getUILang(0, "bgeff" + id.id);
    }
}
