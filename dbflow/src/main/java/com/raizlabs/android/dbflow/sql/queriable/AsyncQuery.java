package com.raizlabs.android.dbflow.sql.queriable;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.BaseAsyncObject;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;

/**
 * Description: Adds async methods to a {@link ModelQueriable}
 */
public class AsyncQuery<TModel extends Model> extends BaseAsyncObject<AsyncQuery<TModel>> {

    private final ModelQueriable<TModel> modelQueriable;
    private QueryTransaction.QueryResultCallback<TModel> queryResultCallback;

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
     * Runs the specified query in the background.
     */
    public void execute() {
        executeTransaction(new QueryTransaction.Builder<>(modelQueriable)
                .queryResult(queryResultCallback).build());
    }

    /**
     * @return The table this Query is associated with.
     */
    public Class<TModel> getTable() {
        return modelQueriable.getTable();
    }

}
