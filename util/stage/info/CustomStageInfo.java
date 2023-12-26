package common.util.stage.info;

import com.google.gson.JsonObject;
import common.CommonStatic;
import common.io.json.JsonClass;
import common.io.json.JsonDecoder;
import common.io.json.JsonField;
import common.io.json.localDecoder;
import common.pack.Identifier;
import common.pack.SortedPackSet;
import common.util.stage.MapColc.PackMapColc;
import common.util.stage.Stage;
import common.util.unit.AbForm;
import common.util.unit.Form;
import common.util.unit.Level;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

@JsonClass
public class CustomStageInfo implements StageInfo {
    private static final DecimalFormat df;

    static {
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        df = (DecimalFormat) nf;

        df.applyPattern("#.##");
    }

    @JsonField(alias = Identifier.class)
    public final Stage st;
    @JsonField(generic = Stage.class, alias = Identifier.class)
    public final ArrayList<Stage> stages = new ArrayList<>();
    @JsonField(generic = Float.class)
    public final ArrayList<Float> chances = new ArrayList<>();
    public short totalChance;
    @JsonField(alias = Form.AbFormJson.class, backCompat = JsonField.CompatType.FORK)
    public Form ubase;
    @JsonField
    public Level lv;
    @JsonField(generic = Form.class, alias = Form.AbFormJson.class, backCompat = JsonField.CompatType.FORK)
    public final SortedPackSet<Form> rewards = new SortedPackSet<>();

    @JsonClass.JCConstructor
    public CustomStageInfo() {
        st = null;
    }

    public CustomStageInfo(Stage st) {
        this.st = st;
        st.info = this;
        if (st.getCont().getCont() instanceof PackMapColc)
            ((PackMapColc)st.getCont().getCont()).si.add(this);
    }

    @Override
    public String getHTML() {
        StringBuilder ans = new StringBuilder();
        ans.append("<html>");
        if (stages.size() > 0) {
            ans.append("<table><tr><th>List of Followup Stages:</th></tr>");
            for (int i = 0; i < stages.size(); i++)
                ans.append("<tr><td>")
                        .append(stages.get(i).getCont().toString())
                        .append(" - ")
                        .append(stages.get(i).toString())
                        .append("</td><td>")
                        .append(df.format(chances.get(i)))
                        .append("%</td></tr>");
        }
        if (ubase != null)
            ans.append("Unit Base: ").append(ubase).append(" (").append(CommonStatic.def.lvText(ubase, lv)[0]).append(")");
        if (rewards.size() > 0) {
            ans.append("<table><tr><th>List of Unit Rewards:</th></tr>");
            for (int i = 0; i < rewards.size(); i++)
                ans.append("<tr><td>").append(rewards.get(i).toString()).append("</td></tr>");
        }
        return ans.toString();
    }

    @Override
    public boolean exConnection() {
        return false;
    }

    @Override
    public Stage[] getExStages() {
        return stages.toArray(new Stage[0]);
    }

    @Override
    public float[] getExChances() {
        float[] FChances = new float[chances.size()];
        for (int i = 0; i < chances.size(); i++)
            FChances[i] = chances.get(i);
        return FChances;
    }

    public void remove(Stage s) {
        remove(stages.indexOf(s));
    }

    public void remove(int ind) {
        if (ind == -1 || ind >= stages.size())
            return;
        stages.remove(ind);
        chances.remove(ind);
        destroy(true);
    }

    public void checkChances() {
        float maxChance = 0;
        for (int i = 0; i < chances.size(); i++)
            maxChance += chances.get(i);
        totalChance = (short)maxChance;

        if (maxChance > 100)
            setTotalChance((byte) 100);
    }

    public void setTotalChance(byte newChance) {
        for (int i = 0; i < chances.size(); i++)
            chances.set(i, (chances.get(i) / totalChance) * newChance);
        totalChance = newChance;
    }

    public void equalizeChances() {
        for (int i = 0; i < chances.size(); i++)
            chances.set(i, (float)totalChance / chances.size());
    }

    /**
     * Called to detach the followup from the stage and remove it from the list storing it
     * @param checkFirst Verify if the StageInfo doesn't have anything that can contribute to the stage before destroying it
     */
    public void destroy(boolean checkFirst) {
        if (checkFirst && (!stages.isEmpty() || ubase != null || !rewards.isEmpty()))
            return;
        stages.clear();
        chances.clear();
        rewards.clear();
        ubase = null;
        ((PackMapColc)st.getCont().getCont()).si.remove(this);
        st.info = null;
    }

    @JsonDecoder.OnInjected
    public void onInjected(JsonObject jobj) {
        st.info = this;
        for (Float chance : chances)
            totalChance += chance;
        if (jobj.has("reward"))
            rewards.add(new localDecoder(jobj.get("reward"), Form.class, this).setAlias(AbForm.AbFormJson.class).decode());
    }
}
