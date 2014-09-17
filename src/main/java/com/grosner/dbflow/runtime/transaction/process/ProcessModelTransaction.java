package com.grosner.dbflow.runtime.transaction.process;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.runtime.DBTransactionInfo;
import com.grosner.dbflow.runtime.transaction.BaseResultTransaction;
import com.grosner.dbflow.runtime.transaction.ResultReceiver;
import com.grosner.dbflow.structure.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Provides a {@link ModelClass}-list backed implementation on the {@link com.grosner.dbflow.runtime.DBTransactionQueue}
 * and allows for specific method calling on a model.
 */
public abstract class ProcessModelTransaction<ModelClass extends Model> extends BaseResultTransaction<ModelClass, List<ModelClass>> {

    protected List<ModelClass> mModels;

    /**
     * Constructs this transaction with a single model enabled.
     * @param dbTransactionInfo The information about this transaction
     * @param mReceiver Will be called when the transaction completes.
     * @param model The single model we wish to act on
     */
    public ProcessModelTransaction(DBTransactionInfo dbTransactionInfo, ResultReceiver<List<ModelClass>> mReceiver,
                                   ModelClass model) {
        super(dbTransactionInfo, mReceiver);
        mModels = new ArrayList<ModelClass>();
        mModels.add(model);
    }

    /**
     * Constructs this transaction with a single model enabled.
     * @param dbTransactionInfo
     * @param mReceiver
     * @param models
     */
    public ProcessModelTransaction(DBTransactionInfo dbTransactionInfo, ResultReceiver<List<ModelClass>> mReceiver,
                                   List<ModelClass> models) {
        super(dbTransactionInfo, mReceiver);
        mModels = models;
    }


    @Override
    public boolean onReady() {
        return mModels != null && !mModels.isEmpty();
    }

    @Override
    public List<ModelClass> onExecute() {
        FlowManager.transact(new Runnable() {
            @Override
            public void run() {
                for (ModelClass model : mModels) {
                    processModel(model);
                }
            }
        });

        return mModels;
    }

    /**
     * Called when we are on the {@link com.grosner.dbflow.runtime.DBTransactionQueue} and looping
     * through the models. Run the specific {@link com.grosner.dbflow.structure.Model} method here.
     * @param model
     */
    protected abstract void processModel(ModelClass model);


}
