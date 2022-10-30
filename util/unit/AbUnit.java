package common.util.unit;

import common.pack.IndexContainer;
import common.pack.PackData;

@IndexContainer.IndexCont(PackData.class)
public interface AbUnit extends Comparable<AbUnit>, IndexContainer.Indexable<PackData, AbUnit> {

    @Override
    default int compareTo(AbUnit u) {
        return getID().compareTo(u.getID());
    }
    Form[] getForms();
}
