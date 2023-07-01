package common.pack;

import common.io.json.JsonClass;
import common.io.json.JsonDecoder.OnInjected;
import common.io.json.JsonField;
import common.util.stage.Stage;
import common.util.stage.StageMap;
import common.util.stage.info.CustomStageInfo;
import common.util.unit.AbForm;
import common.util.unit.AbUnit;
import common.util.unit.Form;
import common.util.unit.Unit;

import java.util.*;

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

    public Collection<?>[] validClear(Stage st) {
        Collection<?>[] flags = new Collection<?>[3];
        ArrayDeque<Form> newForms = new ArrayDeque<>();
        ArrayDeque<Boolean> bForms = new ArrayDeque<>();

        if (st.info instanceof CustomStageInfo)
            for (Form reward : ((CustomStageInfo)st.info).rewards) {
                Integer ind = ulkUni.get(reward.unit);
                if (ind == null || ind < reward.fid) {
                    ulkUni.put(reward.unit, reward.fid);
                    newForms.add(reward);
                    bForms.add(ind != null);
                }
            }

        LinkedList<StageMap> newMaps = new LinkedList<>();
        Integer clm = cSt.get(st.getCont());
        if (clm == null)
            if (st.getCont().unlockReq.isEmpty())
                cSt.put(st.getCont(), clm = 1);
            else
                clm = -1;
        else if (clm == st.getCont().list.indexOf(st))
            cSt.replace(st.getCont(), ++clm);

        if (clm == st.getCont().list.size()) {//StageMap fully cleared
            for (StageMap sm : pack.mc.maps)
                if (sm.unlockReq.size() > 0 && !cSt.containsKey(sm)) {
                    boolean addable = true;
                    for (StageMap smp : sm.unlockReq)
                        if (smp.id.pack.equals(pack.getSID()) && (!cSt.containsKey(smp) || cSt.get(smp) < smp.list.size())) { //Verify if map is there AND cleared first before adding
                            addable = false;
                            break;
                        }
                    if (addable) {
                        cSt.put(sm, 0);
                        newMaps.add(sm);
                    }
                }
            for (PackData.UserPack pac : UserProfile.getUserPacks())
                if (pac.save != null && pac.desc.dependency.contains(pack.getSID()))
                    for (StageMap sm : pac.mc.maps)
                        if (sm.unlockReq.size() > 0 && !pac.save.cSt.containsKey(sm)) {
                            boolean addable = true;
                            for (StageMap smp : sm.unlockReq)
                                if (smp.id.pack.equals(pac.getSID()) && !pac.save.cSt.containsKey(smp)) {
                                    addable = false;
                                    break;
                                }
                            if (addable) {
                                pac.save.cSt.put(sm, 0);
                                newMaps.add(sm);
                            }
                        }
        }
        flags[0] = newForms;
        flags[1] = bForms;
        flags[2] = newMaps;
        return flags;
    }

    public LinkedList<StageMap> getUnlockableMaps(StageMap smap) {
        LinkedList<StageMap> reqMaps = new LinkedList<>();
        for (StageMap sm : pack.mc.maps)
            if (sm.unlockReq.contains(smap))
                reqMaps.add(sm);
        for (PackData.UserPack pac : UserProfile.getUserPacks())
            if (pac.save != null && pac.desc.dependency.contains(pack.getSID()))
                for (StageMap sm : pac.mc.maps)
                    if (sm.unlockReq.contains(smap))
                        reqMaps.add(sm);
        return reqMaps;
    }

    public boolean locked(AbForm f) {
        if (pack.syncPar.contains(f.getID().pack)) {
            PackData.UserPack upack = UserProfile.getUserPack(f.getID().pack);
            if ((upack.defULK.containsKey(f.unit()) && upack.defULK.get(f.unit()) >= f.getFid()) || (upack.save.ulkUni.containsKey(f.unit()) && ulkUni.get(f.unit()) >= f.getFid()))
                return false;
        } else if (f.getID().pack.equals(Identifier.DEF)) {
            for (String par : pack.syncPar) {
                PackData.UserPack upack = UserProfile.getUserPack(par);
                if ((upack.defULK.containsKey(f.unit()) && upack.defULK.get(f.unit()) >= f.getFid()) || (upack.save.ulkUni.containsKey(f.unit()) && ulkUni.get(f.unit()) >= f.getFid()))
                    return false;
            }
        }
        return (!pack.defULK.containsKey(f.unit()) || pack.defULK.get(f.unit()) < f.getFid()) &&
                (!ulkUni.containsKey(f.unit()) || ulkUni.get(f.unit()) < f.getFid());
    }

    @JsonField(tag = "pack", io = JsonField.IOType.W)
    public String zser() {
        return pack.desc.id;
    }

    @OnInjected //Just like every game ever, update save data if something new is added designed for a point below the one you're at
    public void injected() {
        for (StageMap sm : pack.mc.maps)
            if (sm.unlockReq.size() > 0 && !cSt.containsKey(sm)) {
                boolean addable = true;
                for (StageMap smp : sm.unlockReq)
                    if (smp.id.pack.equals(pack.getSID()) && (!cSt.containsKey(smp) || cSt.get(smp) < smp.list.size())) { //Verify if map is there AND cleared first before adding
                        addable = false;
                        break;
                    }
                if (addable)
                    cSt.put(sm, 0);
            }
    }
}
