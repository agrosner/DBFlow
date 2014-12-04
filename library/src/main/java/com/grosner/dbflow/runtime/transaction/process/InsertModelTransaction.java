package com.grosner.dbflow.runtime.transaction.process;

import com.grosner.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class InsertModelTransaction<ModelClass extends Model> extends ProcessModelTransaction<ModelClass> {
    /**
     * Constructs this transaction with a single model enabled.
     *
     * @param modelInfo Holds information about this process request
     */
    public InsertModelTransaction(ProcessModelInfo<ModelClass> modelInfo) {
        super(modelInfo);
    }

    @Override
    public void processModel(ModelClass model) {
        model.insert(false);
    }
}
