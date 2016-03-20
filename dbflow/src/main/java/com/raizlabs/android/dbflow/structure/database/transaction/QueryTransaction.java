package com.raizlabs.android.dbflow.structure.database.transaction;

import com.raizlabs.android.dbflow.sql.queriable.Queriable;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description: Provides an easy way to query for data asynchronously.
 */
public class QueryTransaction implements ITransaction {

    /**
     * Simple interface that provides callback on result.
     *
     * @param <TResult> The result that we got from querying.
     */
    public interface QueryResultCallback<TResult> {

        void onQueryResult(QueryTransaction transaction, TResult tResult);
    }

    final Queriable queriable;

    QueryTransaction(Builder builder) {
        queriable = builder.queriable;
    }

    @Override
    public void execute(DatabaseWrapper databaseWrapper) {

    }

    public static class Builder<TResult> {

        private final Queriable queriable;
        private QueryResultCallback<TResult> queryResultCallback;

        public Builder(Queriable queriable) {
            this.queriable = queriable;
        }

        public Builder queryResult(QueryResultCallback<TResult> queryResultCallback) {
            this.queryResultCallback = queryResultCallback;
            return this;
        }

        public QueryTransaction build() {
            return new QueryTransaction(this);
        }
    }
}
