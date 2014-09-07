package com.raizlabs.android.dbflow.runtime.transaction;

import com.raizlabs.android.dbflow.runtime.DBTransactionInfo;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Provides a base implementation where the {@link com.raizlabs.android.dbflow.runtime.transaction.ResultReceiver}
 * is called, returning a list of {@link ModelClass} objects.
 */
public abstract class BaseResultTransaction<ModelClass extends Model> extends BaseTransaction<List<ModelClass>> {

    protected ResultReceiver<List<ModelClass>> mReceiver;

    public BaseResultTransaction(DBTransactionInfo dbTransactionInfo, ResultReceiver<List<ModelClass>> mReceiver) {
        super(dbTransactionInfo);
        this.mReceiver = mReceiver;
    }

    @Override
    public void onPostExecute(List<ModelClass> modelClasses) {
        if(mReceiver != null) {
            mReceiver.onResultReceived(modelClasses);
        }
    }
}
