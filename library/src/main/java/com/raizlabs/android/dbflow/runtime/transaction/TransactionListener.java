package com.raizlabs.android.dbflow.runtime.transaction;

/**
 * Description: Provides a callback interface to {@link com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction}.
 * This result is on the main UI thread when completed.
 */
public interface TransactionListener<ResultClass> {

    /**
     * The result of the transaction is placed here on the UI thread. For retrievals, it returns the
     * list models or single model item. For any other operation it will return the {@link ResultClass} that was
     * included in the main {@link com.raizlabs.android.dbflow.runtime.transaction.BaseResultTransaction}
     *
     * @param result The result that we will return
     */
    public void onResultReceived(ResultClass result);

    /**
     * Called right before the {@link BaseTransaction#onExecute()} is called. Returning false will not
     * execute the transaction. This is useful for transactions that are not cleaned up.
     *
     * @return true if the transaction is ready to be completed.
     */
    public boolean onReady(BaseTransaction<ResultClass> transaction);

    /**
     * If true, the {@link #onResultReceived(Object)} method is called on the UI thread. If false,
     * we do not pass down a callback.
     *
     * @param result The result that we will return
     * @return
     */
    public boolean hasResult(BaseTransaction<ResultClass> transaction, ResultClass result);

}
