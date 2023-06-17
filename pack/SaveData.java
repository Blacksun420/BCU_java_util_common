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

import java.util.HashMap;
import java.util.TreeMap;

@JsonClass
public class SaveData {

    public final PackData.UserPack pack;
    @JsonField(generic = { Unit.class, Integer.class }, alias = Identifier.class)
    public final TreeMap<AbUnit, Integer> ulkUni = new TreeMap<>();
    @JsonField(generic = { StageMap.class, Integer.class }, alias = Identifier.class)
    public HashMap<StageMap, Integer> cSt = new HashMap<>();//Integer points the number of stages cleared in the map

    public SaveData(PackData.UserPack pack) {
        this.pack = pack;
    }

    public byte validClear(Stage st) {
        Integer clm = cSt.get(st.getCont());
        if (clm == null)
            if (st.getCont().unlockReq.isEmpty())
                cSt.put(st.getCont(), clm = 0);
            else
                return 0;
        else if (clm > st.getCont().list.indexOf(st))
            return 0;
        cSt.replace(st.getCont(), clm + 1);

        byte ret = 1;
        if (st.info instanceof CustomStageInfo && ((CustomStageInfo)st.info).reward != null) {
            Form reward = ((CustomStageInfo)st.info).reward;
            Integer ind = ulkUni.get(reward.unit);
            ulkUni.put(reward.unit, ind == null ? reward.fid : Math.max(ind, reward.fid));
            ret++;
        }
        if (clm + 1 == st.getCont().list.size()) //StageMap fully cleared
            for (StageMap sm : st.getCont().getCont().maps)
                if (!cSt.containsKey(sm)) {
                    boolean addable = true;
                    for (StageMap smp : sm.unlockReq)
                        if (!cSt.containsKey(smp)) {
                            addable = false;
                            break;
                        }
                    if (addable)
                        cSt.put(sm, 0);
                }

        return ret;
    }

    public boolean locked(AbForm f) {
        return (!pack.defULK.containsKey(f.unit()) || pack.defULK.get(f.unit()) < f.getFid()) &&
                (!ulkUni.containsKey(f.unit()) || ulkUni.get(f.unit()) < f.getFid());
    }

    @JsonField(tag = "pack", io = JsonField.IOType.W)
    public String zser() {
        return pack.desc.id;
    }
}
