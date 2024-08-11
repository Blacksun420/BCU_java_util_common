package common.util.stage;

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

    public StageLimit combine(StageLimit second) {
        StageLimit combined = new StageLimit();
        combined.maxMoney = maxMoney == 0 ? second.maxMoney : second.maxMoney == 0 ? maxMoney : Math.min(maxMoney, second.maxMoney);
        combined.globalCooldown = globalCooldown == 0 ? second.globalCooldown : second.globalCooldown == 0 ? globalCooldown : Math.max(globalCooldown, second.globalCooldown);
        combined.coolStart = coolStart || second.coolStart;
        for (int i = 0; i < costMultiplier.length; i++)
            combined.costMultiplier[i] = Math.max(costMultiplier[i], second.costMultiplier[i]);
        for (int i = 0; i < cooldownMultiplier.length; i++)
            combined.cooldownMultiplier[i] = Math.max(cooldownMultiplier[i], second.cooldownMultiplier[i]);
        combined.bannedCatCombo.addAll(bannedCatCombo);
        combined.bannedCatCombo.addAll(second.bannedCatCombo);
        return combined;
    }

    public boolean isBlank() {
        return maxMoney == 0 && globalCooldown == 0 && defCD() && defMoney() && !coolStart && bannedCatCombo.isEmpty();
    }
}
