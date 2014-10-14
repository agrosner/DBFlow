package com.grosner.dbflow.runtime.transaction.process;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.runtime.transaction.BaseResultTransaction;
import com.grosner.dbflow.structure.Model;

import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Provides a {@link ModelClass}-list backed implementation on the {@link com.grosner.dbflow.runtime.DBTransactionQueue}
 * and allows for specific method calling on a model.
 */
public abstract class ProcessModelTransaction<ModelClass extends Model> extends BaseResultTransaction<List<ModelClass>> {

    protected ProcessModelInfo<ModelClass> mModelInfo;

    /**
     * Constructs this transaction with a single model enabled.
     *
     * @param modelInfo Holds information about this process request
     */
    public ProcessModelTransaction(ProcessModelInfo<ModelClass> modelInfo) {
        super(modelInfo.getInfo(), modelInfo.mReceiver);
        mModelInfo = modelInfo;
    }

    @Override
    public boolean onReady() {
        return mModelInfo.hasData();
    }

    @Override
    public List<ModelClass> onExecute() {
        final List<ModelClass> processedModels = mModelInfo.mModels;
        FlowManager.transact(new Runnable() {
            @Override
            public void run() {
                for (ModelClass model : processedModels) {
                    processModel(model);
                }
            }
        });

        return processedModels;
    }

    /**
     * Called when we are on the {@link com.grosner.dbflow.runtime.DBTransactionQueue} and looping
     * through the models. Run a specific {@link com.grosner.dbflow.structure.Model} method here.
     *
     * @param model
     */
    protected abstract void processModel(ModelClass model);


}
