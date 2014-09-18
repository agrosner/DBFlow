package com.grosner.dbflow.runtime.transaction;

import com.grosner.dbflow.runtime.DBTransactionInfo;
import com.grosner.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Provides a base implementation where the {@link com.grosner.dbflow.runtime.transaction.ResultReceiver}
 * is called, returning a {@link ResultClass}.
 */
public abstract class BaseResultTransaction<ModelClass extends Model, ResultClass> extends BaseTransaction<ResultClass> {

    /**
     * The callback to be executed when the transaction completes
     */
    protected ResultReceiver<ResultClass> mReceiver;

    /**
     * Constructs this transaction
     *
     * @param dbTransactionInfo The information about this transaction
     * @param resultReceiver    Will be called when the transaction completes.
     */
    public BaseResultTransaction(DBTransactionInfo dbTransactionInfo, ResultReceiver<ResultClass> resultReceiver) {
        super(dbTransactionInfo);
        this.mReceiver = resultReceiver;
    }

    @Override
    public void onPostExecute(ResultClass modelClasses) {
        if (mReceiver != null) {
            mReceiver.onResultReceived(modelClasses);
        }
    }
}
