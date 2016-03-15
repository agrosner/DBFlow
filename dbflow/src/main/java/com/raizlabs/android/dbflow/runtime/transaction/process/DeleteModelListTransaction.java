package com.raizlabs.android.dbflow.runtime.transaction.process;

import com.raizlabs.android.dbflow.runtime.DefaultTransactionQueue;
import com.raizlabs.android.dbflow.runtime.FlowContentObserver;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: Deletes a list of models passed into this class from the DB.
 */
public class DeleteModelListTransaction<ModelClass extends Model> extends ProcessModelTransaction<ModelClass> {

    /**
     * Constructs this transaction with a {@link ProcessModelInfo} and {@link FlowContentObserver} to wrap the notifications in.
     *
     * @param modelInfo       Holds information about this delete request.
     * @param contentObserver The observer than will begin transaction and end within this transaction on the {@link DefaultTransactionQueue}
     */
    public DeleteModelListTransaction(ProcessModelInfo<ModelClass> modelInfo, FlowContentObserver contentObserver) {
        super(modelInfo, contentObserver);
    }

    /**
     * Constructs this transaction with the specified {@link com.raizlabs.android.dbflow.runtime.transaction.process.ProcessModelInfo}
     *
     * @param modelInfo Holds information about this delete request.
     */
    public DeleteModelListTransaction(ProcessModelInfo<ModelClass> modelInfo) {
        super(modelInfo, null);
    }

    @Override
    public void processModel(ModelClass model) {
        model.delete();
    }
}
