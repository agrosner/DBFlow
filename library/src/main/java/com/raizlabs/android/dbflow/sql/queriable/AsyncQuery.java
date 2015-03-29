package com.raizlabs.android.dbflow.sql.queriable;

import android.database.Cursor;

import com.raizlabs.android.dbflow.runtime.DBTransactionQueue;
import com.raizlabs.android.dbflow.runtime.TransactionManager;
import com.raizlabs.android.dbflow.runtime.transaction.BaseResultTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.SelectListTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.SelectSingleModelTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.TransactionListener;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.List;

/**
 * Description: Adds async methods to a {@link ModelQueriable}
 */
public class AsyncQuery<ModelClass extends Model> {

    private final ModelQueriable<ModelClass> mModelQueriable;
    private final TransactionManager mTransactionManager;

    /**
     * Constructs an instance of this async query.
     *
     * @param queriable          The queriable object to use to query data.
     * @param transactionManager The manager to run this query on
     */
    public AsyncQuery(ModelQueriable<ModelClass> queriable, TransactionManager transactionManager) {
        this.mModelQueriable = queriable;
        this.mTransactionManager = transactionManager;
    }

    /**
     * Queries the list on the {@link DBTransactionQueue}
     *
     * @param transactionListener Listens for transaction events.
     */
    public void queryList(TransactionListener<List<ModelClass>> transactionListener) {
        mTransactionManager.addTransaction(new SelectListTransaction<>(mModelQueriable, transactionListener));
    }

    /**
     * Queries a single item on the {@link DBTransactionQueue}
     *
     * @param transactionListener Listens for transaction events.
     */
    public void querySingle(TransactionListener<ModelClass> transactionListener) {
        mTransactionManager.addTransaction(new SelectSingleModelTransaction<>(mModelQueriable, transactionListener));
    }

    /**
     * @return The table this Query is associated with.
     */
    public Class<ModelClass> getTable() {
        return mModelQueriable.getTable();
    }

    /**
     * Queries the raw {@link Cursor} object from the contained query.
     *
     * @param transactionListener Listens for transaction events.
     */
    public void query(TransactionListener<Cursor> transactionListener) {
        mTransactionManager.addTransaction(new BaseResultTransaction<Cursor>(transactionListener) {
            @Override
            public Cursor onExecute() {
                return mModelQueriable.query();
            }
        });
    }
}
