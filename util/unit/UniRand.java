package common.util.unit;

import common.CommonStatic;
import common.battle.StageBasis;
import common.battle.entity.EUnit;
import common.io.json.JsonClass;
import common.io.json.JsonField;
import common.pack.Identifier;
import common.pack.IndexContainer;
import common.pack.PackData;
import common.system.VImg;
import common.util.BattleObj;
import common.util.Data;
import common.util.unit.rand.UREnt;

import java.util.*;

@JsonClass.JCGeneric(Identifier.class)
@JsonClass
public class UniRand extends Data implements AbForm, IndexContainer.Indexable<PackData, UniRand> {

    public static final byte T_NL = 0, T_LL = 1;

    @JsonField(generic = UREnt.class)
    public final ArrayList<UREnt> list = new ArrayList<>();

    public final Map<StageBasis, ULock> map = new HashMap<>();

    @JsonField
    public int type = 0;

    public void updateCopy(StageBasis sb, Object o) {
        if (o != null)
            map.put(sb, (ULock) o);
    }

    protected UREnt getSelection(StageBasis sb, Object obj) {
        if (type != T_NL) {
            ULock l = map.get(sb);
            if (l == null)
                map.put(sb, l = type == T_LL ? new ULockLL() : new ULockGL());
            UREnt ae = l.get(obj);
            if (ae == null)
                l.put(obj, ae = selector(sb));
            return ae;
        }
        return selector(sb);

    }

    private UREnt selector(StageBasis sb) {
        int tot = 0;
        for (UREnt e : list)
            tot += e.share;
        if (tot > 0) {
            int r = (int) (sb.r.nextDouble() * tot);
            for (UREnt ent : list) {
                r -= ent.share;
                if (r < 0)
                    return ent;
            }
        }
        return null;
    }

    @JsonClass.JCIdentifier
    @JsonField
    public final Identifier<UniRand> id;

    @JsonField
    public String name = "";

    @JsonClass.JCConstructor
    public UniRand() {
        id = null;
    }

    public UniRand(Identifier<UniRand> ID) {
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
            AbForm ae = Identifier.get(e.ent);
            if (ae instanceof Form)
                se.add(e.ent);
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

    public EUnit get(StageBasis sb, Object obj, int minLayer, int maxLayer) {
        UREnt sel = getSelection(sb, obj);
        return new EForm(sel.ent, sel.lv).invokeEntity(sb, minLayer, maxLayer);
    }

    @Override
    public Identifier<AbForm> getID() {
        return id;
    }

    @Override
    public VImg getIcon() {
        return CommonStatic.getBCAssets().ico[0][0];
    }

    public boolean contains(Form form, Form origin) {
        return false;
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