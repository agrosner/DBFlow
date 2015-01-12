package com.raizlabs.android.dbflow.runtime.transaction.process;

import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: Deletes a list of models passed into this class from the DB.
 */
public class DeleteModelListTransaction<ModelClass extends Model> extends ProcessModelTransaction<ModelClass> {

    /**
     * Constructs this transaction with the specified {@link com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo}
     *
     * @param modelInfo Holds information about this delete request.
     */
    public DeleteModelListTransaction(ProcessModelInfo<ModelClass> modelInfo) {
        super(modelInfo);
    }

    @Override
    public void processModel(ModelClass model) {
        model.delete(false);
    }
}
