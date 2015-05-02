package com.raizlabs.android.dbflow.runtime.transaction.process;

import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: Saves all of the {@link ModelClass} into the DB in one transaction.
 */
public class SaveModelTransaction<ModelClass extends Model> extends ProcessModelTransaction<ModelClass> {


    /**
     * Constructs this transaction with the specified {@link com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo}
     *
     * @param modelInfo Holds information about this save request.
     */
    public SaveModelTransaction(ProcessModelInfo<ModelClass> modelInfo) {
        super(modelInfo);
    }

    @Override
    public void processModel(ModelClass model) {
        model.save();
    }

}
