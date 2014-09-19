package com.grosner.dbflow.runtime.observer;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Observes changes in a specific model and will provide automatic callback to them
 */
public interface ModelObserver<ModelClass extends Model> {

    public Class<ModelClass> getModelClass();

    /**
     * Will be called when the {@link ModelClass} has changed
     *
     * @param flowManager  The database the model comes from
     * @param modelChanged The model that has changed value
     */
    public void onModelChanged(FlowManager flowManager, ModelClass modelChanged);
}
