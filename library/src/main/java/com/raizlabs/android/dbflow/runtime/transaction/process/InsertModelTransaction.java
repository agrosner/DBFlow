package com.raizlabs.android.dbflow.runtime.transaction.process;

import com.raizlabs.android.dbflow.runtime.DBTransactionQueue;
import com.raizlabs.android.dbflow.runtime.FlowContentObserver;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: Inserts a list of models passed to it into the DB.
 */
public class InsertModelTransaction<ModelClass extends Model> extends ProcessModelTransaction<ModelClass> {

    /**
     * Constructs this transaction with a {@link ProcessModelInfo} and {@link FlowContentObserver} to wrap the notifications in.
     *
     * @param modelInfo       Holds information about this insert request.
     * @param contentObserver The observer than will begin transaction and end within this transaction on the {@link DBTransactionQueue}
     */
    public InsertModelTransaction(ProcessModelInfo<ModelClass> modelInfo, FlowContentObserver contentObserver) {
        super(modelInfo, contentObserver);
    }

    /**
     * Constructs this transaction with a {@link ProcessModelInfo} and {@link FlowContentObserver} to wrap the notifications in.
     *
     * @param modelInfo Holds information about this insert request.
     */
    public InsertModelTransaction(ProcessModelInfo<ModelClass> modelInfo) {
        super(modelInfo, null);
    }

    @Override
    public void processModel(ModelClass model) {
        model.insert();
    }
}
