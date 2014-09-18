package com.grosner.dbflow.runtime.transaction.process;

import com.grosner.dbflow.runtime.DBTransactionInfo;
import com.grosner.dbflow.runtime.transaction.ResultReceiver;
import com.grosner.dbflow.structure.Model;

import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Deletes a list of models passed into this class from the DB.
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
