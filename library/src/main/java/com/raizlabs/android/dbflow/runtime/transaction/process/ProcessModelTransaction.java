package com.raizlabs.android.dbflow.runtime.transaction.process;

import com.raizlabs.android.dbflow.runtime.transaction.BaseResultTransaction;
import com.raizlabs.android.dbflow.runtime.FlowContentObserver;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.List;

/**
 * Description: Provides a {@link ModelClass}-list backed implementation on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
 * and allows for specific method calling on a model.
 */
public abstract class ProcessModelTransaction<ModelClass extends Model> extends BaseResultTransaction<List<ModelClass>>
        implements ProcessModel<ModelClass> {

    protected ProcessModelInfo<ModelClass> processModelInfo;

    private final FlowContentObserver contentObserver;

    /**
     * Constructs this transaction with a single model enabled.
     *
     * @param modelInfo       Holds information about this process request
     * @param contentObserver The optional {@link FlowContentObserver} to wrap the process in a transaction.
     */
    public ProcessModelTransaction(ProcessModelInfo<ModelClass> modelInfo, FlowContentObserver contentObserver) {
        super(modelInfo.getInfo(), modelInfo.transactionListener);
        processModelInfo = modelInfo;
        this.contentObserver = contentObserver;
    }

    @Override
    public boolean onReady() {
        return processModelInfo.hasData();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<ModelClass> onExecute() {
        if (contentObserver != null) {
            contentObserver.beginTransaction();
        }
        processModelInfo.processModels(this);
        List<ModelClass> models = processModelInfo.models;
        if (contentObserver != null) {
            contentObserver.endTransactionAndNotify();
        }
        return models;
    }

    /**
     * Called when we are on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue} and looping
     * through the models. Run a specific {@link com.raizlabs.android.dbflow.structure.Model} method here.
     *
     * @param model
     */
    @Override
    public abstract void processModel(ModelClass model);

}
