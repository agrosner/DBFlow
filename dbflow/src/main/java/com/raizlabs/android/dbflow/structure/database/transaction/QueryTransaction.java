package com.raizlabs.android.dbflow.structure.database.transaction;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.language.CursorResult;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

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

        void onQueryResult(QueryTransaction transaction, CursorResult<TResult> tResult);
    }

    final ModelQueriable<TResult> modelQueriable;
    final QueryResultCallback<TResult> queryResultCallback;

    QueryTransaction(Builder<TResult> builder) {
        modelQueriable = builder.modelQueriable;
        queryResultCallback = builder.queryResultCallback;
    }

    @Override
    public void execute(DatabaseWrapper databaseWrapper) {
        CursorResult<TResult> cursorResult = modelQueriable.queryResults();
        if (queryResultCallback != null) {
            queryResultCallback.onQueryResult(this, cursorResult);
        }
    }

    public static class Builder<TResult extends Model> {

        private final ModelQueriable<TResult> modelQueriable;
        private QueryResultCallback<TResult> queryResultCallback;

        public Builder(@NonNull ModelQueriable<TResult> modelQueriable) {
            this.modelQueriable = modelQueriable;
        }

        public Builder queryResult(QueryResultCallback<TResult> queryResultCallback) {
            this.queryResultCallback = queryResultCallback;
            return this;
        }

        public QueryTransaction build() {
            return new QueryTransaction<>(this);
        }
    }
}
