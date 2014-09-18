package com.grosner.dbflow.runtime.transaction.process;

import com.grosner.dbflow.runtime.DBTransactionInfo;
import com.grosner.dbflow.runtime.transaction.ResultReceiver;
import com.grosner.dbflow.structure.Model;

import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Updates all of the {@link ModelClass} in one transaction.
 */
public class UpdateModelListTransaction<ModelClass extends Model> extends ProcessModelTransaction<ModelClass> {

    /**
     * Constructs this transaction with a list of models.
     *
     * @param dbTransactionInfo The information about this transaction
     * @param resultReceiver    Will be called when the transaction completes.
     * @param models            The list of models to act on
     */
    public UpdateModelListTransaction(DBTransactionInfo dbTransactionInfo, ResultReceiver<List<ModelClass>> resultReceiver,
                                      List<ModelClass> models) {
        super(dbTransactionInfo, resultReceiver, models);
    }

    @Override
    protected void processModel(ModelClass model) {
        model.update(false);
    }
}
