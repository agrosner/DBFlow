package com.grosner.dbflow.runtime.observer;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.sql.SqlUtils;
import com.grosner.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Observes changes in a specific model and will provide automatic callback to them
 */
public interface ModelObserver<ModelClass extends Model> {

    /**
     * Describes the mode of the save when the model is acted on
     */
    public static enum Mode {

        /**
         * If the model exists, we update, otherwise we insert
         */
        DEFAULT,

        /**
         * We had updated the model
         */
        UPDATE,

        /**
         * We had deleted the model
         */
        DELETE,

        /**
         * We had inserted the model
         */
        INSERT;

        public static Mode fromData(int saveMode, boolean isDelete) {
            Mode mode = null;
            if(isDelete) {
                mode = Mode.DELETE;
            } else {
                switch (saveMode) {
                    case SqlUtils.SAVE_MODE_UPDATE:
                        mode = Mode.UPDATE;
                        break;
                    case SqlUtils.SAVE_MODE_INSERT:
                        mode = Mode.INSERT;
                        break;
                    case SqlUtils.SAVE_MODE_DEFAULT:
                        mode = Mode.DEFAULT;
                }
            }

            return mode;
        }
    }

    public Class<ModelClass> getModelClass();

    /**
     * Will be called when the {@link ModelClass} has changed
     *
     * @param flowManager The database the model comes from
     * @param model       The model that has changed value
     * @param mode        The SQL mode we used {@link com.grosner.dbflow.runtime.observer.ModelObserver.Mode}
     */
    public void onModelChanged(FlowManager flowManager, ModelClass model, Mode mode);
}
