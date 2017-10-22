package com.raizlabs.android.dbflow.structure.database.transaction;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.runtime.BaseTransactionManager;

/**
 * Description: The main transaction class. It represents a transaction that occurs in the database.
 * This is a handy class that allows you to wrap up a set of database modification (or queries) into
 * a code block that gets accessed all on the same thread, in the same queue. This can prevent locking
 * and synchronization issues when trying to read and write from the database at the same time.
 * <p/>
 * To create one, the recommended method is to use the {@link DatabaseDefinition#beginTransactionAsync(ITransaction)}.
 */
public final class Transaction {

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
        void onError(@NonNull Transaction transaction,
                     @NonNull Throwable error);
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
        void onSuccess(@NonNull Transaction transaction);
    }

    private static Handler TRANSACTION_HANDLER;

    static Handler getTransactionHandler() {
        if (TRANSACTION_HANDLER == null) {
            TRANSACTION_HANDLER = new Handler(Looper.getMainLooper());
        }
        return TRANSACTION_HANDLER;
    }


    final Error errorCallback;
    final Success successCallback;
    final ITransaction transaction;
    final DatabaseDefinition databaseDefinition;
    final String name;
    final boolean shouldRunInTransaction;
    final boolean runCallbacksOnSameThread;


    Transaction(Builder builder) {
        databaseDefinition = builder.databaseDefinition;
        errorCallback = builder.errorCallback;
        successCallback = builder.successCallback;
        transaction = builder.transaction;
        name = builder.name;
        shouldRunInTransaction = builder.shouldRunInTransaction;
        runCallbacksOnSameThread = builder.runCallbacksOnSameThread;
    }

    @Nullable
    public Error error() {
        return errorCallback;
    }

    @Nullable
    public Success success() {
        return successCallback;
    }

    @NonNull
    public ITransaction transaction() {
        return transaction;
    }

    @Nullable
    public String name() {
        return name;
    }

    /**
     * Runs the transaction in the {@link BaseTransactionManager} of the associated database.
     */
    public void execute() {
        databaseDefinition.getTransactionManager().addTransaction(this);
    }

    /**
     * Cancels a transaction that has not run yet.
     */
    public void cancel() {
        databaseDefinition.getTransactionManager().cancelTransaction(this);
    }

    /**
     * Executes the transaction immediately on the same thread from which it is called. This calls
     * the {@link DatabaseDefinition#executeTransaction(ITransaction)} method, which runs the
     * {@link #transaction()} in a database transaction.
     */
    public void executeSync() {
        try {
            if (shouldRunInTransaction) {
                databaseDefinition.executeTransaction(transaction);
            } else {
                transaction.execute(databaseDefinition.getWritableDatabase());
            }
            if (successCallback != null) {
                if (runCallbacksOnSameThread) {
                    successCallback.onSuccess(this);
                } else {
                    getTransactionHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            successCallback.onSuccess(Transaction.this);
                        }
                    });
                }
            }
        } catch (final Throwable throwable) {
            FlowLog.INSTANCE.logError(throwable);
            if (errorCallback != null) {
                if (runCallbacksOnSameThread) {
                    errorCallback.onError(this, throwable);
                } else {
                    getTransactionHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            errorCallback.onError(Transaction.this, throwable);
                        }
                    });
                }
            } else {
                throw new RuntimeException("An exception occurred while executing a transaction", throwable);
            }
        }
    }

    @NonNull
    public Builder newBuilder() {
        return new Builder(transaction, databaseDefinition)
            .error(errorCallback)
            .success(successCallback)
            .name(name)
            .shouldRunInTransaction(shouldRunInTransaction)
            .runCallbacksOnSameThread(runCallbacksOnSameThread);
    }

    /**
     * The main entry point into {@link Transaction}, this provides an easy way to build up transactions.
     */
    public static final class Builder {

        final ITransaction transaction;
        @NonNull final DatabaseDefinition databaseDefinition;
        Error errorCallback;
        Success successCallback;
        String name;
        boolean shouldRunInTransaction = true;
        private boolean runCallbacksOnSameThread;


        /**
         * @param transaction        The interface that actually executes the transaction.
         * @param databaseDefinition The database this transaction will run on. Should be the same
         *                           DB as the code that the transaction runs in.
         */
        public Builder(@NonNull ITransaction transaction, @NonNull DatabaseDefinition databaseDefinition) {
            this.transaction = transaction;
            this.databaseDefinition = databaseDefinition;
        }

        /**
         * Specify an error callback to return all and any {@link Throwable} that occured during a {@link Transaction}.
         */
        @NonNull
        public Builder error(@Nullable Error errorCallback) {
            this.errorCallback = errorCallback;
            return this;
        }

        /**
         * Specify a listener for successful transactions. This is called when the {@link ITransaction}
         * specified is finished and it is posted on the UI thread.
         *
         * @param successCallback The callback, invoked on the UI thread.
         */
        @NonNull
        public Builder success(@Nullable Success successCallback) {
            this.successCallback = successCallback;
            return this;
        }

        /**
         * Give this transaction a name. This will allow you to call {@link ITransactionQueue#cancel(String)}.
         *
         * @param name The name of this transaction. Should be unique for any transaction currently
         *             running in the {@link ITransactionQueue}.
         */
        @NonNull
        public Builder name(@Nullable String name) {
            this.name = name;
            return this;
        }

        /**
         * @param shouldRunInTransaction True is default. If true, we run this {@link Transaction} in
         *                               a database transaction. If this is not necessary (usually for
         *                               {@link QueryTransaction}), you should specify false.
         * @return
         */
        @NonNull
        public Builder shouldRunInTransaction(boolean shouldRunInTransaction) {
            this.shouldRunInTransaction = shouldRunInTransaction;
            return this;
        }

        /**
         * @param runCallbacksOnSameThread Default is false. If true we return the callbacks from
         *                                 this {@link Transaction} on the same thread we call
         *                                 {@link #execute()} from.
         */
        @NonNull
        public Builder runCallbacksOnSameThread(boolean runCallbacksOnSameThread) {
            this.runCallbacksOnSameThread = runCallbacksOnSameThread;
            return this;
        }

        /**
         * @return A new instance of {@link Transaction}. Subsequent calls to this method produce
         * new instances.
         */
        @NonNull
        public Transaction build() {
            return new Transaction(this);
        }

        /**
         * Convenience method to simply execute a transaction.
         */
        public void execute() {
            build().execute();
        }
    }
}
