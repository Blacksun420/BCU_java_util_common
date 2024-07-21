package common.util.stage;

import common.CommonStatic;
import common.io.json.JsonClass;
import common.io.json.JsonField;
import common.util.BattleStatic;
import common.util.Data;

import java.util.HashSet;
import java.util.List;

@JsonClass
public class StageLimit extends Data implements BattleStatic {
    @JsonField
    public int maxMoney = 0;
    @JsonField
    public int globalCooldown = 0;
    @JsonField(generic = Integer.class)
    public HashSet<Integer> bannedCatCombo = new HashSet<>();

    public StageLimit() {
    }

    public StageLimit(int maxMoney, int globalCooldown, List<Integer> bannedCombo) {
        this.maxMoney = maxMoney;
        this.globalCooldown = globalCooldown;
        this.bannedCatCombo.addAll(bannedCombo);
    }

    public StageLimit clone() {
        StageLimit sl = new StageLimit();
        sl.maxMoney = maxMoney;
        sl.globalCooldown = globalCooldown;
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
}
