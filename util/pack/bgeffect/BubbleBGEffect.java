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
import java.util.List;
import java.util.Random;

@JsonClass.JCGeneric(BackgroundEffect.BGIdentifier.class)
@SuppressWarnings("ForLoopReplaceableByForEach")
public class BubbleBGEffect implements BackgroundEffect {
    private final Identifier<BackgroundEffect> id;
    private final FakeImage bubble;

    private final int bw;
    private final int bh;

    private final List<P> bubblePosition = new ArrayList<>();
    private final List<Byte> differentiator = new ArrayList<>();
    private final Random r = new Random();

    private final List<Integer> capture = new ArrayList<>();

    public BubbleBGEffect(Identifier<BackgroundEffect> i, FakeImage bubble) {
        id = i;
        this.bubble = bubble;

        bw = (int) (this.bubble.getWidth() * 1.8);
        bh = (int) (this.bubble.getHeight() * 1.8);
    }

    @Override
    public void check() {
        bubble.bimg();
    }

    @Override
    public void preDraw(FakeGraphics g, P rect, double siz, double midH) {

    }

    @Override
    public void postDraw(FakeGraphics g, P rect, double siz, double midH) {
        for(int i = 0; i < bubblePosition.size(); i++) {
            g.drawImage(
                    bubble,
                    BackgroundEffect.convertP(bubblePosition.get(i).x + Data.BG_EFFECT_BUBBLE_FACTOR * Math.sin(differentiator.get(i) + bubblePosition.get(i).y / Data.BG_EFFECT_BUBBLE_STABILIZER), siz) + (int) rect.x,
                    (int) (bubblePosition.get(i).y * siz - rect.y + midH * siz),
                    bw * siz, bh * siz
            );
        }
    }

    @Override
    public void draw(FakeGraphics g, double x, double y, double siz, int groundH, int skyH) {
        for(int i = 0; i < bubblePosition.size(); i++) {
            g.drawImage(
                    bubble,
                    BackgroundEffect.convertP(bubblePosition.get(i).x + Data.BG_EFFECT_BUBBLE_FACTOR * Math.sin(differentiator.get(i) + bubblePosition.get(i).y / Data.BG_EFFECT_BUBBLE_STABILIZER), siz) + (int) x,
                    (int) (bubblePosition.get(i).y * siz - y + skyH * siz),
                    bw * siz, bh * siz
            );
        }
    }

    @Override
    public void update(int w, double h, double midH) {
        capture.clear();

        for(int i = 0; i < bubblePosition.size(); i++) {
            if(bubblePosition.get(i).y < -bh) {
                capture.add(i);
            } else {
                bubblePosition.get(i).y -= (BGHeight * 3.0 + bh) / Data.BG_EFFECT_BUBBLE_TIME;
            }
        }

        if(!capture.isEmpty()) {
            for(int i = 0; i < capture.size(); i++) {
                P.delete(bubblePosition.get(capture.get(i)));

                bubblePosition.set(capture.get(i), P.newP(r.nextInt(w + battleOffset), BGHeight * 3));
                differentiator.set(capture.get(i), (byte) (3 - r.nextInt(6)));
            }
        }
    }

    @Override
    public void initialize(int w, double h, double midH, Background bg) {
        for(int i = 0; i < bubblePosition.size(); i++) {
            P.delete(bubblePosition.get(i));
        }

        bubblePosition.clear();
        differentiator.clear();

        int number = w / 200 - (r.nextInt(w) / 1000);

        for(int i = 0; i < number; i++) {
            bubblePosition.add(P.newP(r.nextInt(w + battleOffset), r.nextDouble() * (BGHeight * 3.0 + bh)));
            differentiator.add((byte) (3 - r.nextInt(6)));
        }
    }

    @Override
    public String toString() {
        return CommonStatic.def.getBtnName(0, "bgeff" + id.id);
    }

    @Override
    public Identifier<BackgroundEffect> getID() {
        return id;
    }
}
