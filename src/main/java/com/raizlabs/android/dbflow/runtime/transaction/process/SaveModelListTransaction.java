package com.raizlabs.android.dbflow.runtime.transaction.process;

import com.raizlabs.android.dbflow.runtime.DBTransactionInfo;
import com.raizlabs.android.dbflow.runtime.transaction.ResultReceiver;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Saves all of the {@link ModelClass} into the DB in one transaction.
 */
public class SaveModelListTransaction<ModelClass extends Model> extends ProcessModelTransaction<ModelClass> {


    public SaveModelListTransaction(DBTransactionInfo transactionInfo, ResultReceiver<List<ModelClass>> resultReceiver,
                                    List<ModelClass> models) {
        super(transactionInfo, resultReceiver, models);
    }

    @Override
    protected void processModel(ModelClass model) {
        model.save(false);
    }

}
