package com.raizlabs.android.dbflow.runtime.transaction.process;

import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: Simple interface for acting on a model in a Transaction or list of {@link com.raizlabs.android.dbflow.structure.Model}
 */
public interface ProcessModel<ModelClass extends Model> {

    /**
     * Called when processing models
     *
     * @param model The model to process
     */
    public void processModel(ModelClass model);
}
