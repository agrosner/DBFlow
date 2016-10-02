package com.raizlabs.android.dbflow.sql.queriable;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.BaseAsyncObject;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;

/**
 * Description: Adds async methods to a {@link ModelQueriable}
 */
public class AsyncQuery<TModel> extends BaseAsyncObject<AsyncQuery<TModel>> {

    private final ModelQueriable<TModel> modelQueriable;
    private QueryTransaction.QueryResultCallback<TModel> queryResultCallback;
    private QueryTransaction.QueryResultListCallback<TModel> queryResultListCallback;
    private QueryTransaction.QueryResultSingleCallback<TModel> queryResultSingleCallback;

    /**
     * Constructs an instance of this async query.
     *
     * @param queriable The queriable object to use to query data.
     */
    public AsyncQuery(@NonNull ModelQueriable<TModel> queriable) {
        super(queriable.getTable());
        this.modelQueriable = queriable;
    }

    /**
     * @param queryResultCallback Called when query is executed and has a result.
     */
    public AsyncQuery<TModel> queryResultCallback(QueryTransaction.QueryResultCallback<TModel> queryResultCallback) {
        this.queryResultCallback = queryResultCallback;
        return this;
    }

    /**
     * @param queryResultSingleCallback Called when query is executed and has a result.
     */
    public AsyncQuery<TModel> querySingleResultCallback(QueryTransaction.QueryResultSingleCallback<TModel> queryResultSingleCallback) {
        this.queryResultSingleCallback = queryResultSingleCallback;
        return this;
    }

    /**
     * @param queryResultListCallback Called when query is executed and has a result.
     */
    public AsyncQuery<TModel> queryListResultCallback(QueryTransaction.QueryResultListCallback<TModel> queryResultListCallback) {
        this.queryResultListCallback = queryResultListCallback;
        return this;
    }

    /**
     * Runs the specified query in the background.
     */
    public void execute() {
        executeTransaction(new QueryTransaction.Builder<>(modelQueriable)
                .queryResult(queryResultCallback)
                .queryListResult(queryResultListCallback)
                .querySingleResult(queryResultSingleCallback)
                .build());
    }

    /**
     * @return The table this Query is associated with.
     */
    public Class<TModel> getTable() {
        return modelQueriable.getTable();
    }

}
