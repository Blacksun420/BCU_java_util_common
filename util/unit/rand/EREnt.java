package common.util.unit.rand;

import common.util.unit.AbEnemy;
import common.io.json.JsonClass;
import common.pack.Identifier;
import common.system.Copable;
import common.util.BattleStatic;
import org.jetbrains.annotations.Nullable;

@JsonClass(noTag = JsonClass.NoTag.LOAD)
public class EREnt implements BattleStatic, Copable<EREnt> {

    @Nullable
    public Identifier<AbEnemy> ent;
    public int multi = 100;
    public int mula = 100;
    public int share = 1;

    @Override
    public EREnt copy() {
        EREnt ans = new EREnt();
        ans.ent = ent;
        ans.multi = multi;
        ans.mula = mula;
        ans.share = share;
        return ans;
    }
}
