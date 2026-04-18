package common.util.stage.info;

import common.util.stage.Stage;

public interface StageInfo {

    Stage getStage();

    boolean exConnection();

    Stage[] getExStages();

    float[] getExChances();
}