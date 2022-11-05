package common.util.pack.bgeffect;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import common.CommonStatic;
import common.io.json.JsonClass;
import common.pack.Identifier;
import common.pack.UserProfile;
import common.system.P;
import common.system.fake.FakeGraphics;
import common.system.files.VFile;
import common.util.Data;
import common.util.pack.Background;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@JsonClass.JCGeneric(BackgroundEffect.BGIdentifier.class)
@SuppressWarnings("ForLoopReplaceableByForEach")
public class JsonBGEffect implements BackgroundEffect {
    private final Identifier<BackgroundEffect> id;
    private final int jid;
    private final List<BGEffectHandler> handlers = new ArrayList<>();

    public JsonBGEffect(Identifier<BackgroundEffect> identifier, int bgID) throws IOException {
        id = identifier;
        jid = bgID;
        String jsonName = "bg"+ Data.trio(jid)+".json";

        VFile vf = VFile.get("./org/data/"+jsonName);

        if(vf == null) {
            throw new FileNotFoundException("Such json file not found : ./org/data/"+jsonName);
        }

        Reader r = new InputStreamReader(vf.getData().getStream(), StandardCharsets.UTF_8);

        JsonElement elem = JsonParser.parseReader(r);

        r.close();

        JsonObject obj = elem.getAsJsonObject();

        if(obj.has("data")) {
            JsonArray arr = obj.getAsJsonArray("data");

            for(int i = 0; i < arr.size(); i++) {
                BGEffectSegment segment = new BGEffectSegment(arr.get(i).getAsJsonObject(), jsonName, jid);
                handlers.add(new BGEffectHandler(segment, jid));
            }
        } else if (obj.has("id")) {
            int efID = obj.get("id").getAsInt();

            for (BackgroundEffect bge : UserProfile.getBCData().bgEffects)
                if (bge instanceof JsonBGEffect && ((JsonBGEffect)bge).jid == efID) {
                    handlers.addAll(((JsonBGEffect)bge).handlers);
                    break;
                }
        }
    }

    @Override
    public void check() {
        for(int i = 0; i < handlers.size(); i++) {
            handlers.get(i).check();
        }
    }

    @Override
    public void preDraw(FakeGraphics g, P rect, double siz, double midH) {
        for(int i = 0; i < handlers.size(); i++)
            handlers.get(i).draw(g, rect, siz, false);
    }

    @Override
    public void postDraw(FakeGraphics g, P rect, double siz, double midH) {
        for(int i = 0; i < handlers.size(); i++)
            handlers.get(i).draw(g, rect, siz, true);
    }

    @Override
    public void draw(FakeGraphics g, double x, double y, double siz, int groundH, int skyH) {
        //FIXME
        siz *= 0.8;
        P pee = new P(x, 1530 * siz - skyH);
        for(int i = 0; i < handlers.size(); i++) {
            handlers.get(i).draw(g, pee, siz, false);
            handlers.get(i).draw(g, pee, siz, true);
        }
    }

    @Override
    public void update(int w, double h, double midH) {
        for(int i = 0; i < handlers.size(); i++) {
            handlers.get(i).update(w, h, midH);
        }
    }

    @Override
    public void initialize(int w, double h, double midH, Background bg) {
        for(int i = 0; i < handlers.size(); i++) {
            handlers.get(i).initialize(w, h, midH);
        }
    }

    @Override
    public void release() {
        for(int i = 0; i < handlers.size(); i++) {
            handlers.get(i).release();
        }
    }

    @Override
    public Identifier<BackgroundEffect> getID() {
        return id;
    }

    @Override
    public String toString() {
        String temp = CommonStatic.def.getBtnName(0, "bgjson" + BackgroundEffect.jsonList.get(id.id - 10));

        if (temp.equals("bgjson" + BackgroundEffect.jsonList.get(id.id - 10)))
            temp = CommonStatic.def.getBtnName(0, "bgeffdum").replace("_", "" + BackgroundEffect.jsonList.get(id.id - 10));
        return temp;
    }
}
