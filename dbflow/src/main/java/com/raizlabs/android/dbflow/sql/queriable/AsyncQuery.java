package com.raizlabs.android.dbflow.sql.queriable;

import android.database.Cursor;

import com.raizlabs.android.dbflow.runtime.DBTransactionInfo;
import com.raizlabs.android.dbflow.runtime.DBTransactionQueue;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.QueryTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.SelectListTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.SelectSingleModelTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.TransactionListener;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.List;

/**
 * Description: Adds async methods to a {@link ModelQueriable}
 */
public class AsyncQuery<ModelClass extends Model> {

    private final ModelQueriable<ModelClass> modelQueriable;
    private final TransactionManager transactionManager;
    private BaseTransaction currentTransaction;

    /**
     * Constructs an instance of this async query.
     *
     * @param queriable          The queriable object to use to query data.
     * @param transactionManager The manager to run this query on
     */
    public AsyncQuery(ModelQueriable<ModelClass> queriable, TransactionManager transactionManager) {
        this.modelQueriable = queriable;
        this.transactionManager = transactionManager;
    }

    /**
     * Runs the specified query in the background.
     */
    public void execute() {
        cancel();
        transactionManager.addTransaction(currentTransaction = new QueryTransaction(DBTransactionInfo.create(), modelQueriable));
    }

    /**
     * Queries the list on the {@link DBTransactionQueue}
     *
     * @param transactionListener Listens for transaction events.
     */
    public void queryList(TransactionListener<List<ModelClass>> transactionListener) {
        cancel();
        transactionManager.addTransaction(currentTransaction = new SelectListTransaction<>(modelQueriable, transactionListener));
    }

    /**
     * Queries a single item on the {@link DBTransactionQueue}
     *
     * @param transactionListener Listens for transaction events.
     */
    public void querySingle(TransactionListener<ModelClass> transactionListener) {
        cancel();
        transactionManager.addTransaction(currentTransaction = new SelectSingleModelTransaction<>(modelQueriable, transactionListener));
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
     * @param transactionListener Listens for transaction events.
     */
    public void query(TransactionListener<Cursor> transactionListener) {
        cancel();
        transactionManager.addTransaction(
                currentTransaction = new QueryTransaction(DBTransactionInfo.create(), modelQueriable, transactionListener));
    }

    public void cancel() {
        if (currentTransaction != null) {
            transactionManager.cancelTransaction(currentTransaction);
            currentTransaction = null;
        }
    }
}
