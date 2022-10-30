package common.util.unit;

import common.CommonStatic;
import common.battle.StageBasis;
import common.util.BattleObj;
import common.util.Data;
import common.util.unit.rand.UREnt;
import common.battle.entity.EUnit;
import common.io.json.JsonClass;
import common.io.json.JsonDecoder;
import common.io.json.JsonField;
import common.pack.Identifier;
import common.pack.Source;
import common.pack.UserProfile;
import common.system.VImg;
import org.jcodec.common.tools.MathUtil;

import java.util.*;

@JsonClass.JCGeneric(Identifier.class)
@JsonClass
public class UniRand extends Data implements AbUnit, AbForm {

    @JsonClass.JCIdentifier
    @JsonField
    public final Identifier<AbUnit> id;

    @JsonField
    public String name = "";
    public VImg icon = null, deployIcon = null;
    @JsonField
    public int price = 50;

    public static final byte T_NL = 0, T_LL = 1;

    @JsonField(generic = UREnt.class)
    public final ArrayList<UREnt> list = new ArrayList<>();

    public final Map<StageBasis, ULock> map = new HashMap<>();

    @JsonField
    public int type = 0;

    @JsonClass.JCConstructor
    public UniRand() {
        id = null;
    }

    public UniRand(Identifier<AbUnit> ID) {
        id = ID;
    }

    @Override
    public Form[] getForms() {
        LinkedList<Form> ents = new LinkedList<>();
        fillPossible(ents, new TreeSet<>());
        return ents.toArray(new Form[0]);
    }

    public void fillPossible(LinkedList<Form> se, Set<UniRand> sr) {
        list.removeIf(er -> er.ent == null);

        sr.add(this);
        for (UREnt e : list) {
            AbForm ae = e.ent;
            if (ae instanceof Form)
                se.add((Form) e.ent);
            if (ae instanceof UniRand) {
                UniRand er = (UniRand) ae;
                if (!sr.contains(er))
                    er.fillPossible(se, sr);
            }
        }
    }

    @Override
    public String toString() {
        return id.id + " - " + name + " (" + id.pack + ")";
    }

    public EUnit getEntity(StageBasis sb, Object obj, int limit, int minLayer, int maxLayer, int lv, int res, int[] index) {
        UREnt sel = getSelection(sb, obj, limit);
        if (sel == null)
            return null;

        if (sel.ent instanceof UniRand)
            return ((UniRand)sel.ent).getEntity(sb, obj, limit, minLayer, maxLayer, lv, res, index);
        Form f = (Form)sel.ent;
        lv = MathUtil.clip(lv + sel.lv.getLv(), -f.unit.max - f.unit.maxp, f.unit.max + f.unit.maxp);
        lv *= (100.0 - res) / 100;
        Level lvl = sel.lv.clone();
        lvl.setLv(lv);

        return new EForm(f, lvl).invokeEntity(sb, minLayer, maxLayer, index);
    }

    @Override
    public Identifier<AbUnit> getID() {
        return id;
    }

    @Override
    public int getDefaultPrice(int sta) {
        return (int) (price * (1 + sta * 0.5));
    }

    public void updateCopy(StageBasis sb, Object o) {
        if (o != null)
            map.put(sb, (ULock) o);
    }

    private UREnt getSelection(StageBasis sb, Object obj, int limit) {
        if (type != T_NL) {
            ULock l = map.get(sb);
            if (l == null)
                map.put(sb, l = type == T_LL ? new ULockLL() : new ULockGL());
            UREnt ae = l.get(obj);
            if (ae == null)
                l.put(obj, ae = selector(sb, limit));
            return ae;
        }
        return selector(sb, limit);

    }

    private UREnt selector(StageBasis sb, int limit) {
        int tot = 0;
        ArrayList<UREnt> limL = getWithinLimit(limit);

        for (UREnt e : limL)
            tot += e.share;
        if (tot > 0) {
            int r = (int) (sb.r.nextDouble() * tot);
            for (UREnt ent : limL) {
                r -= ent.share;
                if (r < 0)
                    return ent;
            }
        }
        return null;
    }

    private ArrayList<UREnt> getWithinLimit(int limit) {
        ArrayList<UREnt> lList = new ArrayList<>(list);
        for (int i = 0; i < lList.size(); i++)
            if (lList.get(i).ent instanceof Form && ((Form)lList.get(i).ent).du.getWill() > limit)
                lList.remove(i--);

        return lList;
    }

    @Override
    public VImg getIcon() {
        if (icon != null)
            return icon;
        return CommonStatic.getBCAssets().ico[0][0];
    }

    @Override
    public VImg getDeployIcon() {
        if (deployIcon != null)
            return deployIcon;
        return CommonStatic.getBCAssets().slot[0];
    }

    /*public boolean contains(Form f, Form origin) {
        for(UREnt id : list) {
            Form i = id.ent;

            if(i == null)
                continue;

            if(origin.equals(i))
                continue;

            if (i.getID().pack.equals(f.getID().pack) && i.getID() == f.getID())
                return true;
        }

        return false;
    }*/

    @JsonDecoder.OnInjected
    public void onInjected() {
        icon = UserProfile.getUserPack(id.pack).source.readImage(Source.BasePath.RAND + "/unitDisplayIcons", id.id);
        deployIcon = UserProfile.getUserPack(id.pack).source.readImage(Source.BasePath.RAND + "/unitDeployIcons", id.id);
    }
}

interface ULock {

    UREnt get(Object obj);

    UREnt put(Object obj, UREnt ae);

}

class ULockGL extends BattleObj implements ULock {

    private UREnt ae;

    @Override
    public UREnt get(Object obj) {
        return ae;
    }

    @Override
    public UREnt put(Object obj, UREnt e) {
        UREnt pre = ae;
        ae = e;
        return pre;
    }

}

class ULockLL extends HashMap<Object, UREnt> implements ULock {

    private static final long serialVersionUID = 1L;

}