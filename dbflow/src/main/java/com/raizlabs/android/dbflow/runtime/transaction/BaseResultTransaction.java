package com.raizlabs.android.dbflow.runtime.transaction;

import com.raizlabs.android.dbflow.runtime.DBTransactionInfo;

/**
 * Description: Provides a base implementation where the {@link TransactionListener}
 * is called, returning a {@link ResultClass}.
 */
public abstract class BaseResultTransaction<ResultClass> extends BaseTransaction<ResultClass> {

    /**
     * The callback to be executed when the transaction completes
     */
    private TransactionListener<ResultClass> transactionListener;

    /**
     * Constructs this transaction with the default {@link com.raizlabs.android.dbflow.runtime.DBTransactionInfo}
     *
     * @param transactionListener Will be called when the transaction completes.
     */
    public BaseResultTransaction(TransactionListener<ResultClass> transactionListener) {
        this(DBTransactionInfo.create(), transactionListener);
    }

    /**
     * Constructs this transaction
     *
     * @param dbTransactionInfo   The information about this transaction
     * @param transactionListener Will be called when the transaction completes.
     */
    public BaseResultTransaction(DBTransactionInfo dbTransactionInfo, TransactionListener<ResultClass> transactionListener) {
        super(dbTransactionInfo);
        this.transactionListener = transactionListener;
    }

    @Override
    public boolean hasResult(ResultClass result) {
        return transactionListener != null && transactionListener.hasResult(this, result);
    }

    @Override
    public void onPostExecute(ResultClass modelClasses) {
        if (transactionListener != null) {
            transactionListener.onResultReceived(modelClasses);
        }
    }

    @Override
    public boolean onReady() {
        return transactionListener != null && transactionListener.onReady(this);
    }
}
