package com.raizlabs.android.dbflow.runtime.transaction;

/**
 * Author: andrewgrosner
 * Description: The new interface for callbacks to use. Only requires you to implement {@link #onResultReceived(Object)}.
 * It provides a unified listener for transactions and enables more-powerful callbacks.
 * Replaces the deprecated {@link com.raizlabs.android.dbflow.runtime.transaction.ResultReceiver}
 */
public abstract class TransactionListenerAdapter<ResultClass> extends ResultReceiver<ResultClass> {

    @Override
    public abstract void onResultReceived(ResultClass resultClass);

    @Override
    public boolean onReady(BaseTransaction<ResultClass> transaction) {
        return super.onReady(transaction);
    }

    @Override
    public boolean hasResult(BaseTransaction<ResultClass> transaction, ResultClass result) {
        return super.hasResult(transaction, result);
    }
}
