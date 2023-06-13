package common.pack;

import common.io.json.JsonClass;
import common.io.json.JsonField;
import common.util.stage.StageMap;
import common.util.unit.AbUnit;
import common.util.unit.Level;

import java.util.TreeMap;

@JsonClass(noTag = JsonClass.NoTag.LOAD)
public class SaveData {

    @JsonClass(noTag = JsonClass.NoTag.LOAD)
    public static class UnitSaveData {
        public Level lv;
        public int forms;
    }

    @JsonField
    public int XP;
    @JsonField(generic = { Identifier.class, Level.class })
    public final TreeMap<Identifier<AbUnit>, Level> map = new TreeMap<>();
    @JsonField(generic = { StageMap.class, Integer.class })
    public TreeMap<Identifier<StageMap>, Integer> clearedStages = new TreeMap<>();
}
