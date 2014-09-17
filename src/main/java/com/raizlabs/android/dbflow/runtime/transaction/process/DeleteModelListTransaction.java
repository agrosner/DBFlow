package com.raizlabs.android.dbflow.runtime.transaction.process;

import com.raizlabs.android.dbflow.runtime.DBTransactionInfo;
import com.raizlabs.android.dbflow.runtime.transaction.ResultReceiver;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Deletes models passed into this class from the DB.
 */
public class DeleteModelListTransaction<ModelClass extends Model> extends ProcessModelTransaction<ModelClass> {
    public DeleteModelListTransaction(DBTransactionInfo dbTransactionInfo, ResultReceiver<List<ModelClass>> mReceiver,
                                         List<ModelClass> models) {
        super(dbTransactionInfo, mReceiver, models);
    }

    @Override
    protected void processModel(ModelClass model) {
        model.delete(false);
    }
}
