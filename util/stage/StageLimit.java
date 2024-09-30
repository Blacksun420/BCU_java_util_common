package common.util.stage;

import common.io.json.JsonClass;
import common.io.json.JsonField;
import common.util.BattleStatic;
import common.util.Data;

import java.util.HashSet;

@JsonClass(noTag = JsonClass.NoTag.LOAD)
public class StageLimit extends Data implements BattleStatic {
    public int maxMoney = 0;
    public int globalCooldown = 0;
    public int globalCost = 0;

    public int[] cooldownMultiplier = { 100, 100, 100, 100, 100, 100 };
    public int[] costMultiplier = { 100, 100, 100, 100, 100, 100 };
    public int[] rarityDeployLimit = { -1, -1, -1, -1, -1, -1 }; // -1 for none

    public boolean coolStart = false;

    @JsonField(generic = Integer.class)
    public HashSet<Integer> bannedCatCombo = new HashSet<>();

    public StageLimit() {

    }

    public StageLimit clone() {
        StageLimit sl;

        try {
            sl = (StageLimit) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();

            sl = new StageLimit();
        }

        sl.maxMoney = maxMoney;
        sl.globalCooldown = globalCooldown;
        sl.bannedCatCombo.addAll(bannedCatCombo);
        return sl;
    }

    public StageLimit combine(StageLimit second) {
        StageLimit combined = new StageLimit();
        combined.maxMoney = second.maxMoney == 0 ? maxMoney : second.maxMoney;
        combined.globalCooldown = second.globalCooldown == 0 ? globalCooldown : second.globalCooldown;
        combined.globalCost = second.globalCost == 0 ? globalCost : second.globalCost;
        combined.bannedCatCombo.addAll(bannedCatCombo);
        combined.bannedCatCombo.addAll(second.bannedCatCombo);
        return combined;
    }
}