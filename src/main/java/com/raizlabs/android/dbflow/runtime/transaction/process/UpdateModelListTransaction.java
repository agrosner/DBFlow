package com.raizlabs.android.dbflow.runtime.transaction.process;

import com.raizlabs.android.dbflow.runtime.DBTransactionInfo;
import com.raizlabs.android.dbflow.runtime.transaction.ResultReceiver;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Updates all of the {@link ModelClass} in one transaction.
 */
public class UpdateModelListTransaction<ModelClass extends Model> extends ProcessModelTransaction<ModelClass> {
    public UpdateModelListTransaction(DBTransactionInfo dbTransactionInfo, ResultReceiver<List<ModelClass>> mReceiver, List<ModelClass> models) {
        super(dbTransactionInfo, mReceiver, models);
    }

    @Override
    protected void processModel(ModelClass model) {
        model.update(false);
    }
}
