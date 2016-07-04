package com.raizlabs.android.dbflow.structure.database.transaction;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.sql.language.CursorResult;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import java.util.List;

/**
 * Description: Provides an easy way to query for data asynchronously.
 */
public class QueryTransaction<TResult extends Model> implements ITransaction {

    /**
     * Simple interface that provides callback on result.
     *
     * @param <TResult> The result that we got from querying.
     */
    public interface QueryResultCallback<TResult extends Model> {

        /**
         * Called when the query completes.
         *
         * @param transaction The transaction that ran.
         * @param tResult     The result of the query. Use this object to get data that you need.
         */
        void onQueryResult(QueryTransaction transaction, @NonNull CursorResult<TResult> tResult);
    }

    /**
     * Simple interface that provides {@link List} callback on result.
     *
     * @param <TResult> The result that we got from querying.
     */
    public interface QueryResultListCallback<TResult extends Model> {

        /**
         * Called when the query completes.
         *
         * @param transaction The transaction that ran.
         * @param tResult     The {@link List} result of the query.
         */
        void onListQueryResult(QueryTransaction transaction, @Nullable List<TResult> tResult);
    }

    /**
     * Simple interface that provides single {@link TResult} callback on result.
     *
     * @param <TResult> The result that we got from querying.
     */
    public interface QueryResultSingleCallback<TResult extends Model> {

        /**
         * Called when the query completes.
         *
         * @param transaction The transaction that ran.
         * @param tResult     The single result of the query.
         */
        void onSingleQueryResult(QueryTransaction transaction, @Nullable TResult tResult);
    }

    final ModelQueriable<TResult> modelQueriable;
    final QueryResultCallback<TResult> queryResultCallback;
    final QueryResultListCallback<TResult> queryResultListCallback;
    final QueryResultSingleCallback<TResult> queryResultSingleCallback;
    final boolean runResultCallbacksOnSameThread;

    QueryTransaction(Builder<TResult> builder) {
        modelQueriable = builder.modelQueriable;
        queryResultCallback = builder.queryResultCallback;
        queryResultListCallback = builder.queryResultListCallback;
        queryResultSingleCallback = builder.queryResultSingleCallback;
        runResultCallbacksOnSameThread = builder.runResultCallbacksOnSameThread;
    }

    @Override
    public void execute(DatabaseWrapper databaseWrapper) {
        final CursorResult<TResult> cursorResult = modelQueriable.queryResults();
        if (queryResultCallback != null) {
            if (runResultCallbacksOnSameThread) {
                queryResultCallback.onQueryResult(this, cursorResult);
            } else {
                Transaction.getTransactionHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        queryResultCallback.onQueryResult(QueryTransaction.this, cursorResult);
                    }
                });
            }
        }

        if (queryResultListCallback != null) {
            final List<TResult> resultList = cursorResult.toListClose();
            if (runResultCallbacksOnSameThread) {
                queryResultListCallback.onListQueryResult(this, resultList);
            } else {
                Transaction.getTransactionHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        queryResultListCallback.onListQueryResult(QueryTransaction.this, resultList);
                    }
                });
            }
        }

        if (queryResultSingleCallback != null) {
            final TResult result = cursorResult.toModelClose();
            if (runResultCallbacksOnSameThread) {
                queryResultSingleCallback.onSingleQueryResult(this, result);
            } else {
                Transaction.getTransactionHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        queryResultSingleCallback.onSingleQueryResult(QueryTransaction.this, result);
                    }
                });
            }
        }
    }

    /**
     * Provides easy way to build a {@link QueryTransaction}.
     *
     * @param <TResult>
     */
    public static final class Builder<TResult extends Model> {

        final ModelQueriable<TResult> modelQueriable;
        QueryResultCallback<TResult> queryResultCallback;
        QueryResultListCallback<TResult> queryResultListCallback;
        QueryResultSingleCallback<TResult> queryResultSingleCallback;
        boolean runResultCallbacksOnSameThread;

        public Builder(@NonNull ModelQueriable<TResult> modelQueriable) {
            this.modelQueriable = modelQueriable;
        }

        /**
         * Called when transaction completes and use this to get results.
         */
        public Builder<TResult> queryResult(QueryResultCallback<TResult> queryResultCallback) {
            this.queryResultCallback = queryResultCallback;
            return this;
        }

        /**
         * Called when transaction completes, which returns a {@link List} result.
         */
        public Builder<TResult> queryListResult(QueryResultListCallback<TResult> queryResultListCallback) {
            this.queryResultListCallback = queryResultListCallback;
            return this;
        }

        /**
         * Called when transaction completes, which returns a single {@link TResult}.
         */
        public Builder<TResult> querySingleResult(QueryResultSingleCallback<TResult> queryResultSingleCallback) {
            this.queryResultSingleCallback = queryResultSingleCallback;
            return this;
        }

        /**
         * Runs result callback on UI thread by default. setting this to true would run the callback
         * synchrously on the same thread this transaction executes from.
         */
        public Builder<TResult> runResultCallbacksOnSameThread(boolean runResultCallbacksOnSameThread) {
            this.runResultCallbacksOnSameThread = runResultCallbacksOnSameThread;
            return this;
        }

        /**
         * @return A new {@link QueryTransaction}. Subsequent calls to this method produce new
         * instances.
         */
        public QueryTransaction<TResult> build() {
            return new QueryTransaction<>(this);
        }
    }
}
