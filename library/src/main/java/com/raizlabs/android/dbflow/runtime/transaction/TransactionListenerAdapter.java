package com.raizlabs.android.dbflow.runtime.transaction;

/**
 * Description: A base implementation of {@link com.raizlabs.android.dbflow.runtime.transaction.TransactionListener} .
 * Only requires you to implement {@link #onResultReceived(Object)}.
 * It provides a unified listener for transactions and enables more-powerful callbacks.
 */
public class TransactionListenerAdapter<ResultClass> implements TransactionListener<ResultClass> {

    @Override
    public void onResultReceived(ResultClass resultClass) {

    }

    @Override
    public boolean onReady(BaseTransaction<ResultClass> transaction) {
        return true;
    }

    @Override
    public boolean hasResult(BaseTransaction<ResultClass> transaction, ResultClass result) {
        return true;
    }
}
