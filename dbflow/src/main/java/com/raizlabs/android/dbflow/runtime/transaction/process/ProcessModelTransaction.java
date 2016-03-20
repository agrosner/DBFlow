package com.raizlabs.android.dbflow.runtime.transaction.process;

import com.raizlabs.android.dbflow.runtime.DefaultTransactionQueue;
import com.raizlabs.android.dbflow.runtime.FlowContentObserver;
import com.raizlabs.android.dbflow.runtime.transaction.BaseResultTransaction;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.List;

/**
 * Description: Provides a {@link ModelClass}-list backed implementation on the {@link DefaultTransactionQueue}
 * and allows for specific method calling on a model.
 */
public abstract class ProcessModelTransaction<ModelClass extends Model> extends BaseResultTransaction<List<ModelClass>>
        implements com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction.ProcessModel<ModelClass> {

    /**
     * Called during execution of the {@link #processModel(Model)} method. Gives an idea of progress.
     */
    public interface OnProcessProgressChangeListener<ModelClass> {

        /**
         * @param current       The current index of execution.
         * @param maxProgress   The maximum progress (total count of models)
         * @param modifiedModel The model that was modified.
         */
        void onProcessProgressChange(long current, long maxProgress, ModelClass modifiedModel);
    }

    protected ProcessModelInfo<ModelClass> processModelInfo;

    private final FlowContentObserver contentObserver;

    private long count = 0;
    private final long totalCount;

    private OnProcessProgressChangeListener<ModelClass> changeListener;

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

        totalCount = processModelInfo.models.size();
    }

    /**
     * Registers a listener to get callbacks during the {@link #processModel(Model)} operation when this
     * transaction is executed.
     *
     * @param changeListener The listener to call.
     */
    public void setChangeListener(OnProcessProgressChangeListener<ModelClass> changeListener) {
        this.changeListener = changeListener;
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
        processModelInfo.processModels(new com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction.ProcessModel<ModelClass>() {
            @Override
            public void processModel(ModelClass model) {
                ProcessModelTransaction.this.processModel(model);
                count++;

                if (changeListener != null) {
                    changeListener.onProcessProgressChange(count, totalCount, model);
                }
            }
        });
        List<ModelClass> models = processModelInfo.models;
        if (contentObserver != null) {
            contentObserver.endTransactionAndNotify();
        }
        return models;
    }

    /**
     * Called when we are on the {@link DefaultTransactionQueue} and looping
     * through the models. Run a specific {@link com.raizlabs.android.dbflow.structure.Model} method here.
     *
     * @param model
     */
    @Override
    public abstract void processModel(ModelClass model);

}
