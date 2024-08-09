package common.util.stage;

import common.CommonStatic;
import common.io.json.JsonClass;
import common.io.json.JsonField;
import common.util.BattleStatic;
import common.util.Data;

import java.util.HashSet;

@JsonClass(noTag = JsonClass.NoTag.LOAD)
public class StageLimit extends Data implements BattleStatic {

    @JsonField(defval = "0")
    public int maxMoney = 0, globalCooldown = 0;
    @JsonField(defval = "this.defCD")
    public int[] cooldownMultiplier = { 100, 100, 100, 100, 100, 100 };
    @JsonField(defval = "this.defMoney")
    public int[] costMultiplier = { 100, 100, 100, 100, 100, 100 };
    @JsonField(defval = "false")
    public boolean coolStart = false;
    @JsonField(generic = Integer.class, defval = "isEmpty")
    public HashSet<Integer> bannedCatCombo = new HashSet<>();

    public boolean defCD() {
        for (int cd : cooldownMultiplier)
            if (cd != 100)
                return false;
        return true;
    }
    public boolean defMoney() {
        for (int cd : cooldownMultiplier)
            if (cd != 100)
                return false;
        return true;
    }

    public StageLimit() {
    }

    public StageLimit clone() {
        StageLimit sl = new StageLimit();
        sl.maxMoney = maxMoney;
        sl.globalCooldown = globalCooldown;
        sl.cooldownMultiplier = cooldownMultiplier.clone();
        sl.costMultiplier = costMultiplier.clone();
        sl.bannedCatCombo.addAll(bannedCatCombo);
        return sl;
    }

    public String getHTML() {
        StringBuilder ans = new StringBuilder();
        if (!bannedCatCombo.isEmpty()) {
            String[] comboData = new String[bannedCatCombo.size()];
            ans.append("<br> Banned combos: ");
            int i = 0;
            for (int id : bannedCatCombo)
                comboData[i++] = CommonStatic.def.getUILang(2, "nb" + id);
            ans.append(String.join(", ", comboData));
        }
        if (maxMoney > 0)
            ans.append("<br> Total Bank: ").append(maxMoney);
        if (globalCooldown > 0)
            ans.append("<br> Universal CD: ").append(globalCooldown);
        return ans.toString();
    }

    public StageLimit combine(StageLimit second) {
        StageLimit combined = new StageLimit();
        combined.maxMoney = second.maxMoney == 0 ? maxMoney : second.maxMoney;
        combined.globalCooldown = second.globalCooldown == 0 ? globalCooldown : second.globalCooldown;
        combined.bannedCatCombo.addAll(bannedCatCombo);
        combined.bannedCatCombo.addAll(second.bannedCatCombo);
        return combined;
    }
}
