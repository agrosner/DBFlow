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

        /**
         * Called when the query completes.
         *
         * @param transaction The transaction that ran.
         * @param tResult     The result of the query. Use this object to get data that you need.
         */
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

    /**
     * Provides easy way to build a {@link QueryTransaction}.
     *
     * @param <TResult>
     */
    public static final class Builder<TResult extends Model> {

        final ModelQueriable<TResult> modelQueriable;
        QueryResultCallback<TResult> queryResultCallback;

        /**
         * @param modelQueriable      The SQLite wrapper class that you wish to query with.
         *                            EX. SQLite.select().from(SomeTable.class).where(...)
         * @param queryResultCallback called when the result completes.
         */
        public Builder(@NonNull ModelQueriable<TResult> modelQueriable, QueryResultCallback<TResult> queryResultCallback) {
            this.modelQueriable = modelQueriable;
            this.queryResultCallback = queryResultCallback;
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
