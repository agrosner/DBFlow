package com.raizlabs.android.dbflow.sql.queriable;

import android.database.Cursor;

import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

/**
 * Description: Adds async methods to a {@link ModelQueriable}
 */
public class AsyncQuery<ModelClass extends Model> {

    private final ModelQueriable<ModelClass> modelQueriable;
    private Transaction currentTransaction;
    private DatabaseDefinition database;

    /**
     * Constructs an instance of this async query.
     *
     * @param queriable                 The queriable object to use to query data.
     */
    public AsyncQuery(ModelQueriable<ModelClass> queriable) {
        this.modelQueriable = queriable;
        database = FlowManager.getDatabaseForTable(queriable.getTable());
    }

    /**
     * Runs the specified query in the background.
     */
    public void execute() {
        cancel();
        currentTransaction = database
            .beginTransactionAsync(new QueryTransaction.Builder<>(modelQueriable).build())
            .build();
        currentTransaction.execute();
    }

    /**
     * @return The table this Query is associated with.
     */
    public Class<ModelClass> getTable() {
        return modelQueriable.getTable();
    }

    /**
     * Queries the raw {@link Cursor} object from the contained query.
     *
     * @param queryResultCallback Called when query succeeds.
     * @param error               Passed if any errors occur during transaction.
     */
    public void query(QueryTransaction.QueryResultCallback<ModelClass> queryResultCallback,
                      Transaction.Error error) {
        cancel();

        currentTransaction = database
            .beginTransactionAsync(new QueryTransaction.Builder<>(modelQueriable)
                .queryResult(queryResultCallback).build())
            .error(error).build();
        currentTransaction.execute();
    }

    public void cancel() {
        if (currentTransaction != null) {
            database.getTransactionManager().cancelTransaction(currentTransaction);
            currentTransaction = null;
        }
    }
}
