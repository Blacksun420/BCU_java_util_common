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
        public int maxForm;
    }
    @JsonField(generic = { Identifier.class, UnitSaveData.class })
    public final TreeMap<Identifier<AbUnit>, UnitSaveData> ulckUni = new TreeMap<>();
    @JsonField(generic = { StageMap.class, Integer.class })
    public TreeMap<Identifier<StageMap>, Integer> cSt = new TreeMap<>();//Integer points the number of stages cleared in the map
}
