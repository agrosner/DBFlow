package com.raizlabs.android.dbflow.structure.database.transaction;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

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
         * @param ITransaction The transaction that failed.
         * @param error        The error that was thrown.
         */
        void onError(ITransaction ITransaction, Throwable error);
    }

    /**
     * Interface callback when a {@link ITransaction} was successful.
     */
    public interface Success {

        /**
         * Called when a transaction succeeded.
         *
         * @param ITransaction The transacton that succeeded.
         */
        void onSuccess(ITransaction ITransaction);
    }

    final Error errorCallback;
    final Success successCallback;
    final ITransaction transaction;

    Transaction(Builder builder) {
        errorCallback = builder.errorCallback;
        successCallback = builder.successCallback;
        transaction = builder.transaction;
    }

    public void execute() {

    }

    public static final class Builder {

        final ITransaction transaction;
        @NonNull final DatabaseWrapper databaseWrapper;
        Error errorCallback;
        Success successCallback;

        public Builder(@NonNull ITransaction transaction, @NonNull DatabaseWrapper databaseWrapper) {
            this.transaction = transaction;
            this.databaseWrapper = databaseWrapper;
        }

        public Builder error(Error errorCallback) {
            this.errorCallback = errorCallback;
            return this;
        }

        public Builder success(Success successCallback) {
            this.successCallback = successCallback;
            return this;
        }

        public Transaction build() {
            return new Transaction(this);
        }
    }
}
