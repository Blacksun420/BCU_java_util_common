package common.util.pack.bgeffect;

import common.CommonStatic;
import common.io.json.JsonClass;
import common.pack.Identifier;
import common.pack.UserProfile;
import common.system.P;
import common.system.fake.FakeGraphics;
import common.system.fake.FakeImage;
import common.system.fake.FakeTransform;
import common.util.Data;
import common.util.pack.Background;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@JsonClass.JCGeneric(Identifier.class)
@SuppressWarnings("ForLoopReplaceableByForEach")
public class RockBGEffect extends BackgroundEffect {
    private FakeImage rock;
    private FakeImage segment;

    private final List<P> rockPosition = new ArrayList<>();
    private final List<Double> speed = new ArrayList<>();
    private final List<Double> size = new ArrayList<>();
    private final List<Boolean> isRock = new ArrayList<>();
    private final List<Double> angle = new ArrayList<>();
    //0 : Behind, 2 : Front
    private final List<Byte> layer = new ArrayList<>();
    private final List<Integer> opacity = new ArrayList<>();

    private final List<Integer> capture = new ArrayList<>();
    private final Random r = new Random();

    boolean vibrate = false;

    public RockBGEffect(Identifier<BackgroundEffect> id) {
        super(id);
    }

    @Override
    public void check() {
        if(rock != null)
            rock.bimg();

        if(segment != null)
            segment.bimg();
    }

    @Override
    public void preDraw(FakeGraphics g, P rect, double siz, double midH) {
        FakeTransform at = g.getTransform();

        for(int i = 0; i < rockPosition.size(); i++) {
            if(layer.get(i) == 0) {
                g.setComposite(FakeGraphics.TRANS, opacity.get(i), 0);

                FakeImage img = isRock.get(i) ? rock : segment;
                double s = size.get(i);

                g.translate(BackgroundEffect.convertP(rockPosition.get(i).x, siz) + (int) rect.x, (int) (rockPosition.get(i).y * siz - rect.y));
                g.rotate(angle.get(i));
                g.drawImage(img, 0, 0, img.getWidth() * s * siz, img.getHeight() * s * siz);

                g.setTransform(at);
            }
        }

        g.setComposite(FakeGraphics.DEF, 255, 0);

        g.setTransform(at);
        g.delete(at);
    }

    @Override
    public void postDraw(FakeGraphics g, P rect, double siz, double midH) {
        FakeTransform at = g.getTransform();

        for(int i = 0; i < rockPosition.size(); i++) {
            if(layer.get(i) == 1) {
                FakeImage img = isRock.get(i) ? rock : segment;
                double s = size.get(i);

                g.translate(BackgroundEffect.convertP(rockPosition.get(i).x + (vibrate ? 2 : -2), siz) + (int) rect.x, (int) (rockPosition.get(i).y * siz - rect.y + midH * siz));
                g.rotate(angle.get(i));
                g.drawImage(img, 0, 0, img.getWidth() * s * siz, img.getHeight() * s * siz);

                g.setTransform(at);
            }
        }

        g.setTransform(at);
        g.delete(at);
    }

    @Override
    public void draw(FakeGraphics g, double y, double siz, double midH) {
        FakeTransform at = g.getTransform();
        for(int i = 0; i < rockPosition.size(); i++) {
            if(layer.get(i) == 0) {
                g.setComposite(FakeGraphics.TRANS, opacity.get(i), 0);

                FakeImage img = isRock.get(i) ? rock : segment;
                double s = size.get(i);

                g.translate(BackgroundEffect.convertP(rockPosition.get(i).x, siz), (int) (rockPosition.get(i).y * siz - y));
                g.rotate(angle.get(i));
                g.drawImage(img, 0, 0, img.getWidth() * s * siz, img.getHeight() * s * siz);

                g.setTransform(at);
            }
        }
        g.setComposite(FakeGraphics.DEF, 255, 0);
        for(int i = 0; i < rockPosition.size(); i++) {
            if(layer.get(i) == 1) {
                FakeImage img = isRock.get(i) ? rock : segment;
                double s = size.get(i);

                g.translate(BackgroundEffect.convertP(rockPosition.get(i).x + (vibrate ? 2 : -2), siz), (int) (rockPosition.get(i).y * siz - y + midH * siz));
                g.rotate(angle.get(i));
                g.drawImage(img, 0, 0, img.getWidth() * s * siz, img.getHeight() * s * siz);

                g.setTransform(at);
            }
        }

        g.setTransform(at);
        g.delete(at);
    }

    @Override
    public void update(int w, double h, double midH) {
        capture.clear();

        vibrate = !vibrate;

        for(int i = 0; i < rockPosition.size(); i++) {
            double s = size.get(i);
            int rh = isRock.get(i) ? rock.getHeight() : segment.getHeight();

            if(rockPosition.get(i).y < -rh * s) {
                capture.add(i);
            } else {
                rockPosition.get(i).y -= speed.get(i);

                if(layer.get(i) != 1&& opacity.get(i) != 255) {
                    opacity.set(i, Math.min(255, opacity.get(i) + 5));
                }
            }
        }

        if(!capture.isEmpty()) {
            for(int i = 0; i < capture.size(); i++) {
                byte l = (byte) (r.nextDouble() >= 1/4.0 ? 0 : 1);
                boolean isR = l != 1 && r.nextDouble() >= 0.9;
                double siz = (Data.BG_EFFECT_ROCK_SIZE[l] - r.nextDouble() * (1 - 0.5 * l)) * (isR ? 0.8 : 1.0);

                int rw = (int) ((isR ? rock.getWidth() : segment.getWidth()) * siz);

                rockPosition.get(capture.get(i)).x = r.nextInt(w + battleOffset + 2 * rw) - rw;
                rockPosition.get(capture.get(i)).y = l == 0 ? 1020 + Data.BG_EFFECT_ROCK_BEHIND_SPAWN_OFFSET : BGHeight * 3;
                isRock.set(capture.get(i), isR);
                angle.set(capture.get(i), r.nextDouble() * Math.PI);
                layer.set(capture.get(i), l);
                size.set(capture.get(i), siz);
                speed.set(capture.get(i), Data.BG_EFFECT_ROCK_SPEED[l] - r.nextDouble() * 0.5);
                opacity.set(capture.get(i), l == 0 ? 0 : 255);
            }
        }
    }

    @Override
    public void initialize(int w, double h, double midH, Background bg) {
        for(int i = 0; i < rockPosition.size(); i++) {
            P.delete(rockPosition.get(i));
        }

        rockPosition.clear();
        speed.clear();
        size.clear();
        isRock.clear();
        angle.clear();
        layer.clear();

        rock = null;
        segment = null;

        Background background;

        if(!bg.id.pack.equals(Identifier.DEF) || (bg.id.id != 41 && bg.id.id != 75)) {
            background = UserProfile.getBCData().bgs.get(75);
        } else {
            background = bg;
        }

        background.load();

        if(background.id.id == 41) {
            rock = background.parts[21];
            segment = background.parts[20];
        } else {
            rock = background.parts[22];
            segment = background.parts[21];
        }

        int number = w / 100;

        for(int i = 0; i < number; i++) {
            byte l = (byte) (r.nextDouble() >= 0.25 ? 0 : 1);
            boolean isR = l != 1 && r.nextDouble() >= 0.9;
            double siz = (Data.BG_EFFECT_ROCK_SIZE[l] - r.nextDouble() * (1 - 0.5 * l)) * (isR ? 0.8 : 1.0);

            int rw = (int) ((isR ? rock.getWidth() : segment.getWidth()) * siz);

            rockPosition.add(P.newP(r.nextInt(w + battleOffset + 2 * rw) - rw, r.nextInt(l == 0 ? 1020 + Data.BG_EFFECT_ROCK_BEHIND_SPAWN_OFFSET : BGHeight * 3)));
            speed.add(Data.BG_EFFECT_ROCK_SPEED[l] - r.nextDouble() * 0.5);
            isRock.add(isR);
            angle.add(r.nextDouble() * Math.PI);
            layer.add(l);
            size.add(siz);
            opacity.add(255);
        }
    }

    @Override
    public String toString() {
        return CommonStatic.def.getUILang(0, "bgeff" + id.id);
    }
}
