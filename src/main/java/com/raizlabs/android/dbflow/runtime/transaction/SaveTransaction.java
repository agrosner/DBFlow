package com.raizlabs.android.dbflow.runtime.transaction;

import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.runtime.DBTransactionInfo;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Saves all of the {@link ModelClass} into the DB in one transaction.
 */
public class SaveTransaction<ModelClass extends Model> extends BaseResultTransaction<ModelClass> {

    private List<ModelClass> mModels;

    public SaveTransaction(DBTransactionInfo transactionInfo, ResultReceiver<List<ModelClass>> resultReceiver,
                           List<ModelClass> models) {
        super(transactionInfo, resultReceiver);
        mModels = models;
    }

    @Override
    public boolean onReady() {
        return mModels != null && !mModels.isEmpty();
    }

    @Override
    public List<ModelClass> onExecute() {
        FlowConfig.transact(new Runnable() {
            @Override
            public void run() {
                for(ModelClass model: mModels) {
                    model.save();
                }
            }
        });

        return mModels;
    }
}
