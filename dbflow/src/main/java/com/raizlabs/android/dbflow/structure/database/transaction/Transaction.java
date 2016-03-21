package com.raizlabs.android.dbflow.structure.database.transaction;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.config.DatabaseDefinition;

/**
 * Description: The main async transaction class. It represents a transaction that occurs in the database.
 */
public class Transaction {

    /**
     * Callback when a {@link ITransaction} failed because of an exception.
     */
    public interface Error {

        /**
         * Called when transaction fails.
         *
         * @param transaction The transaction that failed.
         * @param error       The error that was thrown.
         */
        void onError(Transaction transaction, Throwable error);
    }

    /**
     * Interface callback when a {@link ITransaction} was successful.
     */
    public interface Success {

        /**
         * Called when a transaction succeeded.
         *
         * @param transaction The transaction that succeeded.
         */
        void onSuccess(Transaction transaction);
    }

    private static final Handler TRANSACTION_HANDLER = new Handler(Looper.getMainLooper());


    final Error errorCallback;
    final Success successCallback;
    final ITransaction transaction;
    final DatabaseDefinition databaseDefinition;
    final String name;

    Transaction(Builder builder) {
        databaseDefinition = builder.databaseDefinition;
        errorCallback = builder.errorCallback;
        successCallback = builder.successCallback;
        transaction = builder.transaction;
        name = builder.name;
    }

    public Error error() {
        return errorCallback;
    }

    public Success success() {
        return successCallback;
    }

    public ITransaction transaction() {
        return transaction;
    }

    public String name() {
        return name;
    }

    public void execute() {
        // TODO: place on proper transaction queue.
    }

    public void executeSync() {
        try {
            transaction.execute(databaseDefinition.getWritableDatabase());
            if (successCallback != null) {
                TRANSACTION_HANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        successCallback.onSuccess(Transaction.this);
                    }
                });
            }
        } catch (Throwable throwable) {
            if (errorCallback != null) {
                errorCallback.onError(this, throwable);
            }
        }
    }

    public static final class Builder {

        final ITransaction transaction;
        @NonNull final DatabaseDefinition databaseDefinition;
        Error errorCallback;
        Success successCallback;
        private String name;

        public Builder(@NonNull ITransaction transaction, @NonNull DatabaseDefinition databaseDefinition) {
            this.transaction = transaction;
            this.databaseDefinition = databaseDefinition;
        }

        public Builder error(Error errorCallback) {
            this.errorCallback = errorCallback;
            return this;
        }

        public Builder success(Success successCallback) {
            this.successCallback = successCallback;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Transaction build() {
            return new Transaction(this);
        }
    }
}
