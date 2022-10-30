package common.util.unit;

import common.pack.Identifier;
import common.pack.IndexContainer;
import common.pack.PackData;
import common.system.BasedCopable;
import common.system.VImg;

@IndexContainer.IndexCont(PackData.class)
public interface AbForm {

    VImg getIcon();

    Form[] getForms();
}
