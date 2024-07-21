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
import common.util.unit.UniRand;
import common.util.unit.rand.UREnt;

import java.util.*;

@JsonClass
public class SaveData {

    public final PackData.UserPack pack;
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

    /**
     * Required chapters still left to unlock a chapter
     * @param sm The chapter
     * @return All uncleared but required chapters, empty list if unlocked
     */
    public LinkedList<StageMap> requirements(StageMap sm) {
        LinkedList<StageMap> cl = new LinkedList<>();
        if (sm.unlockReq.isEmpty() || cSt.containsKey(sm))
            return cl; //Chapter is unlocked
        for (StageMap lsm : sm.unlockReq)
            if (!cSt.containsKey(lsm) || cSt.get(lsm) < lsm.list.size())
                cl.add(lsm); //A requirement chapter is uncleared, add
        return cl;
    }

    /**
     * A chapter is locked, but close to unlocking
     * @param sm the chapter
     * @return True if all chapter requirements are unlocked, but this chapter isn't
     */
    public boolean nearUnlock(StageMap sm) {
        if (sm.unlockReq.isEmpty() || cSt.containsKey(sm))
            return false; //Chapter is unlocked
        for (StageMap lsm : sm.unlockReq)
            if (!lsm.unlockReq.isEmpty() && !cSt.containsKey(lsm))
                return false; //A requirement chapter is locked
        return true;
    }

    @JsonField(tag = "pack", io = JsonField.IOType.W)
    public String zser() {
        return pack.desc.id;
    }

    /**
     * Mostly used to lock units, and for startup
     */
    public void resetUnlockedUnits() {
        ulkUni.clear();
        for (StageMap sm : pack.mc.maps)
            if (cSt.containsKey(sm))
                for (int i = 0; i < cSt.get(sm); i++)
                    if (sm.list.get(i).info instanceof CustomStageInfo)
                        for (Form reward : ((CustomStageInfo)sm.list.get(i).info).rewards) {
                            Integer ind = ulkUni.get(reward.unit);
                            if (ind == null || ind < reward.fid)
                                ulkUni.put(reward.unit, reward.fid);
                        }
    }

    public Stage unlockedAt(Form f) {
        for (int i = 0; i < pack.mc.si.size(); i++) {
            CustomStageInfo csi = (CustomStageInfo)pack.mc.si.get(i);
            for (Form ff : csi.rewards)
                if (f.unit == ff.unit && ff.fid >= f.fid)
                    return csi.st;
        }
        return null;
    }

    public HashMap<AbForm, Stage> getUnlockedsBeforeStage(Stage st, boolean includeRandom) {
        HashMap<AbForm, Stage> ulK = new HashMap<>();
        for (int i = st.getCont().list.indexOf(st) - 1; i >= 0; i--)
            if (st.getCont().list.get(i).info instanceof CustomStageInfo) {
                CustomStageInfo csi = (CustomStageInfo)st.getCont().list.get(i).info;
                for (Form f : csi.rewards) {
                    boolean ad = true;
                    for (int j = f.fid + 1; j < f.unit.forms.length; j++)
                        if (ulK.containsKey(f.unit.forms[j])) {
                            ad = false;
                            break;
                        }
                    if (ad)
                        ulK.put(f, st);
                }
            }
        for (StageMap uchp : st.getCont().unlockReq)
            locRec(ulK, uchp);

        if (includeRandom) {
            for (Map.Entry<AbUnit, Integer> u : pack.defULK.entrySet())
                for (int i = 0; i <= u.getValue(); i++)
                    ulK.put(u.getKey().getForms()[i], null);
            getRandsRec(ulK, pack);
        }
        return ulK;
    }

    private static void locRec(HashMap<AbForm, Stage> ulK, StageMap chp) {
        for (int i = chp.list.size() - 1; i >= 0; i--)
            if (chp.list.get(i).info instanceof CustomStageInfo) {
                CustomStageInfo csi = (CustomStageInfo)chp.list.get(i).info;
                for (Form f : csi.rewards) {
                    boolean ad = true;
                    for (int j = f.fid + 1; j < f.unit.forms.length; j++)
                        if (ulK.containsKey(f.unit.forms[j])) {
                            ad = false;
                            break;
                        }
                    if (ad)
                        ulK.put(f, chp.list.get(i));
                }
            }
        for (StageMap uchp : chp.unlockReq)
            locRec(ulK, uchp);
    }
    private static void getRandsRec(HashMap<AbForm, Stage> ulK, PackData.UserPack pk) {
        for (UniRand rand : pk.randUnits)
            getRandRec(ulK, rand);
        for (String par : pk.desc.dependency)
            getRandsRec(ulK, UserProfile.getUserPack(par));
    }
    private static void getRandRec(HashMap<AbForm, Stage> ulK, UniRand r) {
        boolean add = true;
        for (UREnt e : r.list) {
            if (e.ent instanceof UniRand)
                getRandRec(ulK, (UniRand) e.ent);
            if (!ulK.containsKey(e.ent)) {
                add = false;
                break;
            }
        }
        if (add)
            ulK.put(r, null);
    }

    @OnInjected //Just like every game ever, update save data if something new is added designed for a point below the one you're at
    public void injected() {
        resetUnlockedUnits();
        for (StageMap sm : pack.mc.maps)
            if (!sm.unlockReq.isEmpty() && !cSt.containsKey(sm)) {
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
