package com.raizlabs.android.dbflow.sql;

import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.database.transaction.ITransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

/**
 * Description: Internal use to provide common implementation for async objects.
 */
public class BaseAsyncObject<TAsync> {

    private Transaction.Success successCallback;
    private Transaction.Error errorCallback;
    private Transaction currentTransaction;
    private final DatabaseDefinition databaseDefinition;

    public BaseAsyncObject(Class<? extends Model> modelClass) {
        databaseDefinition = FlowManager.getDatabaseForTable(modelClass);
    }

    /**
     * Listen for any errors that occur during operations on this {@link TAsync}.
     */
    @SuppressWarnings("unchecked")
    public TAsync error(Transaction.Error errorCallback) {
        this.errorCallback = errorCallback;
        return (TAsync) this;
    }

    /**
     * Listens for successes on this {@link TAsync}. Will return the {@link Transaction}.
     */
    @SuppressWarnings("unchecked")
    public TAsync success(Transaction.Success success) {
        this.successCallback = success;
        return (TAsync) this;
    }

    /**
     * Cancels current running transaction.
     */
    public void cancel() {
        if (currentTransaction != null) {
            currentTransaction.cancel();
        }
    }

    protected void executeTransaction(ITransaction transaction) {
        cancel();
        currentTransaction = databaseDefinition
                .beginTransactionAsync(transaction)
                .error(error)
                .success(success)
                .build();
        currentTransaction.execute();
    }

    protected void onError(Transaction transaction, Throwable error) {

    }

    protected void onSuccess(Transaction transaction) {

    }

    private final Transaction.Error error = new Transaction.Error() {
        @Override
        public void onError(Transaction transaction, Throwable error) {
            if (errorCallback != null) {
                errorCallback.onError(transaction, error);
            }
            BaseAsyncObject.this.onError(transaction, error);
            currentTransaction = null;
        }
    };

    private final Transaction.Success success = new Transaction.Success() {
        @Override
        public void onSuccess(Transaction transaction) {
            if (successCallback != null) {
                successCallback.onSuccess(transaction);
            }
            BaseAsyncObject.this.onSuccess(transaction);
            currentTransaction = null;
        }
    };
}
