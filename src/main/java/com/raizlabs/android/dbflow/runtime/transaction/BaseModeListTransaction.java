package com.raizlabs.android.dbflow.runtime.transaction;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.runtime.DBTransactionInfo;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Provides a {@link ModelClass}-list backed implementation on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
 * and allows for specificbb method calling on a model.
 */
public abstract class BaseModeListTransaction<ModelClass extends Model> extends BaseResultTransaction<ModelClass> {

    protected List<ModelClass> mModels;

    public BaseModeListTransaction(DBTransactionInfo dbTransactionInfo, ResultReceiver<List<ModelClass>> mReceiver,
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
     * Called when we are on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue} and looping
     * through the models. Run the specific {@link com.raizlabs.android.dbflow.structure.Model} method here.
     * @param model
     */
    protected abstract void processModel(ModelClass model);

}
