package com.raizlabs.android.dbflow.runtime.transaction.process;

import com.raizlabs.android.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Description: Updates all of the {@link ModelClass} in one transaction.
 */
public class UpdateModelListTransaction<ModelClass extends Model> extends ProcessModelTransaction<ModelClass> {

    /**
     * Constructs this transaction with the specified {@link com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo}
     *
     * @param modelInfo Holds information about this save request.
     */
    public UpdateModelListTransaction(ProcessModelInfo<ModelClass> modelInfo) {
        super(modelInfo);
    }

    @Override
    public void processModel(ModelClass model) {
        model.update(false);
    }
}
