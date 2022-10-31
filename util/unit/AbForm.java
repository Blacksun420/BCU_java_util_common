package common.util.unit;

import common.io.json.JsonClass;
import common.pack.Identifier;
import common.system.VImg;

/**
 * Placeholder class to connect RandomUnit with Form
 */
@JsonClass.JCGeneric(AbForm.AbFormJson.class)
@JsonClass(read = JsonClass.RType.FILL)
public interface AbForm {
    @JsonClass(noTag = JsonClass.NoTag.LOAD)
    class AbFormJson {

        public Identifier<AbUnit> uid;
        public int fid;

        @JsonClass.JCConstructor
        public AbFormJson() {
        }

        @JsonClass.JCConstructor
        public AbFormJson(AbForm f) {
            uid = f.getID();
            fid = f.getFid();
        } //TODO - Test if this constructor isn't necessary

        @JsonClass.JCConstructor
        public AbFormJson(UniRand ur) {
            uid = ur.getID();
            fid = 0;
        }

        @JsonClass.JCConstructor
        public AbFormJson(Form f) {
            uid = f.uid;
            fid = f.fid;
        }

        @JsonClass.JCGetter
        public AbForm get() {
            if (uid.get() instanceof UniRand)
                return (UniRand) uid.get();

            try {
                return uid.get().getForms()[fid];
            } catch (Exception e) {
                return null;
            }
        }
    }

    Identifier<AbUnit> getID();

    default int getFid() {
        return 0;
    }

    int getDefaultPrice(int sta);

    VImg getIcon();

    VImg getDeployIcon();
}
