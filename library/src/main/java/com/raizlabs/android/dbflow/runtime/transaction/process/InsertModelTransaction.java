package com.raizlabs.android.dbflow.runtime.transaction.process;

import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: Inserts a list of models passed to it into the DB.
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
