package com.grosner.dbflow.runtime.transaction.process;

import com.grosner.dbflow.runtime.DBTransactionInfo;
import com.grosner.dbflow.runtime.transaction.ResultReceiver;
import com.grosner.dbflow.structure.Model;

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
