package common.pack;

import common.io.json.JsonClass;
import common.io.json.JsonField;
import common.util.stage.Stage;
import common.util.stage.StageMap;
import common.util.stage.info.CustomStageInfo;
import common.util.unit.AbForm;
import common.util.unit.AbUnit;
import common.util.unit.Form;
import common.util.unit.Unit;

import java.util.TreeMap;

@JsonClass(noTag = JsonClass.NoTag.LOAD)
public class SaveData {

    @JsonClass(noTag = JsonClass.NoTag.LOAD)
    public static class DefaultPackSave {
        @JsonField(generic = { Identifier.class, UnitSaveData.class })
        public final TreeMap<Identifier<AbUnit>, Integer> defULK = new TreeMap<>();//Customs unlocked from the get-go
        @JsonField(generic = Unit.class, alias = Identifier.class)
        public final SortedPackSet<AbUnit> BCLockeds = new SortedPackSet<>();//Locked BC units in the pack
    }
    @JsonClass(noTag = JsonClass.NoTag.LOAD)
    public static class UnitSaveData {
        //public Level lv;
        public int maxForm;
        public UnitSaveData(int fid) {
            maxForm = fid;
        }
        public void increaseForm(int f) {
            maxForm = f;
        }
        /*public UnitSaveData(Level lv) {

        }*/
    }
    public final PackData.UserPack pack;
    @JsonField(generic = { Identifier.class, UnitSaveData.class })
    public final TreeMap<Identifier<AbUnit>, UnitSaveData> ulckUni = new TreeMap<>();
    @JsonField(generic = { StageMap.class, Integer.class })
    public TreeMap<Identifier<StageMap>, Integer> cSt = new TreeMap<>();//Integer points the number of stages cleared in the map

    public SaveData(PackData.UserPack pack) {
        this.pack = pack;
    }

    public byte validClear(Stage st) {
        Integer clm = cSt.get(st.getCont().id);
        if (clm == null)
            if (st.getCont().unlockReq.isEmpty())
                cSt.put(st.getCont().id, clm = 0);
            else
                return 0;
        else if (clm > st.getCont().list.indexOf(st))
            return 0;
        cSt.replace(st.getCont().id, clm + 1);

        byte ret = 1;
        if (st.info instanceof CustomStageInfo && ((CustomStageInfo)st.info).reward != null) {
            Form reward = ((CustomStageInfo)st.info).reward;
            if (ulckUni.containsKey(reward.getID()))
                ulckUni.get(reward.getID()).increaseForm(reward.fid);
            else
                ulckUni.put(reward.getID(), new UnitSaveData(reward.fid));
            ret++;
        }
        if (clm + 1 == st.getCont().list.size()) //StageMap fully cleared
            for (StageMap sm : st.getCont().getCont().maps)
                if (!cSt.containsKey(sm.id)) {
                    boolean addable = true;
                    for (Identifier<StageMap> smp : sm.unlockReq)
                        if (!cSt.containsKey(smp)) {
                            addable = false;
                            break;
                        }
                    if (addable)
                        cSt.put(sm.id, 0);
                }

        return ret;
    }

    public boolean locked(AbForm f) {
        return (!f.getID().pack.equals(Identifier.DEF) || pack.defVals.BCLockeds.contains(f.unit())) &&
                (!pack.defVals.defULK.containsKey(f.getID()) || pack.defVals.defULK.get(f.getID()) < f.getFid()) &&
                (!ulckUni.containsKey(f.getID()) || ulckUni.get(f.getID()).maxForm < f.getFid());
    }

    @JsonField(tag = "pack", io = JsonField.IOType.W)
    public String zser() {
        return pack.desc.id;
    }
}
