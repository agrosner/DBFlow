package com.grosner.dbflow.runtime.transaction.process;

import com.grosner.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Deletes a list of models passed into this class from the DB.
 */
public class DeleteModelListTransaction<ModelClass extends Model> extends ProcessModelTransaction<ModelClass> {

    /**
     * Constructs this transaction with the specified {@link com.grosner.dbflow.runtime.transaction.process.ProcessModelInfo}
     *
     * @param modelInfo Holds information about this delete request.
     */
    public DeleteModelListTransaction(ProcessModelInfo<ModelClass> modelInfo) {
        super(modelInfo);
    }

    @Override
    protected void processModel(ModelClass model) {
        model.delete(false);
    }
}
