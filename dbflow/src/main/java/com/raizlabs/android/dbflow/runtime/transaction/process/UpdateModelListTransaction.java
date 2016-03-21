package com.raizlabs.android.dbflow.runtime.transaction.process;

import com.raizlabs.android.dbflow.structure.database.transaction.DefaultTransactionQueue;
import com.raizlabs.android.dbflow.runtime.FlowContentObserver;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: Updates all of the {@link ModelClass} in one transaction.
 */
public class UpdateModelListTransaction<ModelClass extends Model> extends ProcessModelTransaction<ModelClass> {

    /**
     * Constructs this transaction with a {@link ProcessModelInfo} and {@link FlowContentObserver} to wrap the notifications in.
     *
     * @param modelInfo       Holds information about this update request.
     * @param contentObserver The observer than will begin transaction and end within this transaction on the {@link DefaultTransactionQueue}
     */
    public UpdateModelListTransaction(ProcessModelInfo<ModelClass> modelInfo, FlowContentObserver contentObserver) {
        super(modelInfo, contentObserver);
    }

    /**
     * Constructs this transaction with a {@link ProcessModelInfo} and {@link FlowContentObserver} to wrap the notifications in.
     *
     * @param modelInfo Holds information about this update request.
     */
    public UpdateModelListTransaction(ProcessModelInfo<ModelClass> modelInfo) {
        super(modelInfo, null);
    }

    @Override
    public void processModel(ModelClass model) {
        model.update();
    }
}
