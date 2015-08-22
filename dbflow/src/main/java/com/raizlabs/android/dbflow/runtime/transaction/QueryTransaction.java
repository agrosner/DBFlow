package com.raizlabs.android.dbflow.runtime.transaction;

import android.database.Cursor;

import com.raizlabs.android.dbflow.runtime.DBTransactionInfo;
import com.raizlabs.android.dbflow.sql.queriable.Queriable;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Description: Runs a DB query in the BG on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}. It supplies
 * the cursor returned from this query and will automatically closes the cursor to prevent database leaks.
 */
public class QueryTransaction<ModelClass extends Model> extends BaseResultTransaction<Cursor> {

    private Queriable queriable;

    /**
     * Constructs a new instance that will simply run a query as a transaction.
     *
     * @param dbTransactionInfo The information on how to process the transaction
     * @param queriable         The data object that that has certain methods pertaining to queries.
     */
    public QueryTransaction(DBTransactionInfo dbTransactionInfo, Queriable queriable) {
        this(dbTransactionInfo, queriable, null);
    }

    /**
     * Constructs a new instance that provides a listener for this transaction.
     *
     * @param dbTransactionInfo         The information on how to process the transaction
     * @param queriable                 The data object that that has certain methods pertaining to queries.
     * @param cursorTransactionListener The callback that gets invoked that enables processing of the cursor.
     */
    public QueryTransaction(DBTransactionInfo dbTransactionInfo, Queriable queriable, TransactionListener<Cursor> cursorTransactionListener) {
        super(dbTransactionInfo, cursorTransactionListener);
        this.queriable = queriable;
    }

    @Override
    public boolean onReady() {
        return queriable != null;
    }

    @Override
    public Cursor onExecute() {
        return queriable.query();
    }

    @Override
    public void onPostExecute(Cursor cursor) {
        super.onPostExecute(cursor);

        if (cursor != null) {
            cursor.close();
        }
    }
}
