package com.grosner.dbflow.runtime.transaction;

import com.grosner.dbflow.runtime.DBTransactionInfo;
import com.grosner.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Provides a base implementation where the {@link com.grosner.dbflow.runtime.transaction.ResultReceiver}
 * is called, returning a list of {@link ModelClass} objects.
 */
public abstract class BaseResultTransaction<ModelClass extends Model, ResultClass> extends BaseTransaction<ResultClass> {

    protected ResultReceiver<ResultClass> mReceiver;

    public BaseResultTransaction(DBTransactionInfo dbTransactionInfo, ResultReceiver<ResultClass> mReceiver) {
        super(dbTransactionInfo);
        this.mReceiver = mReceiver;
    }

    @Override
    public void onPostExecute(ResultClass modelClasses) {
        if(mReceiver != null) {
            mReceiver.onResultReceived(modelClasses);
        }
    }
}
