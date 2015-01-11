package com.raizlabs.android.dbflow.runtime.transaction;

/**
 * Description: The new interface for callbacks to use. Only requires you to implement {@link #onResultReceived(Object)}.
 * It provides a unified listener for transactions and enables more-powerful callbacks.
 * Replaces the deprecated {@link com.raizlabs.android.dbflow.runtime.transaction.ResultReceiver}
 */
public class TransactionListenerAdapter<ResultClass> extends ResultReceiver<ResultClass> {

    @Override
    public void onResultReceived(ResultClass resultClass) {
        super.onResultReceived(resultClass);
    }

    @Override
    public boolean onReady(BaseTransaction<ResultClass> transaction) {
        return super.onReady(transaction);
    }

    @Override
    public boolean hasResult(BaseTransaction<ResultClass> transaction, ResultClass result) {
        return super.hasResult(transaction, result);
    }
}
