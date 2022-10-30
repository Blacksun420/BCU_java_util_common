package common.util.unit.rand;

import common.io.json.JsonClass;
import common.io.json.JsonField;
import common.system.Copable;
import common.util.BattleStatic;
import common.util.unit.Form;
import common.util.unit.Level;

@JsonClass(noTag = JsonClass.NoTag.LOAD)
public class UREnt implements BattleStatic, Copable<UREnt> {

    @JsonField(alias = Form.FormJson.class)
    public Form ent;
    public Level lv;
    public int share = 1;

    public UREnt(Form f) {
        ent = f;
        lv = new Level(f.getPrefLvs());
    }

    @Override
    public UREnt copy() {
        UREnt ans = new UREnt(ent);
        ans.lv = lv.clone();
        ans.share = share;
        return ans;
    }
}
