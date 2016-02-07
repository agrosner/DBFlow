package com.raizlabs.android.dbflow.runtime.transaction;

import android.database.Cursor;

import com.raizlabs.android.dbflow.runtime.DBTransactionInfo;
import com.raizlabs.android.dbflow.runtime.DBTransactionQueue;
import com.raizlabs.android.dbflow.sql.queriable.Queriable;

/**
 * Description: Runs a DB query in the BG on the {@link DBTransactionQueue}. It supplies
 * the cursor returned from this query and will automatically closes the cursor to prevent database leaks.
 */
public class QueryTransaction extends BaseResultTransaction<Cursor> {

    private final Queriable queriable;
    private final String[] selectionArgs;

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
     * @param selectionArgs             You may include ?s in selection, which will be replaced by the values
     *                                  from selectionArgs, in order that they appear in the selection. The
     *                                  values will be bound as Strings.
     */
    public QueryTransaction(DBTransactionInfo dbTransactionInfo, Queriable queriable, TransactionListener<Cursor> cursorTransactionListener, String... selectionArgs) {
        super(dbTransactionInfo, cursorTransactionListener);
        this.queriable = queriable;
        this.selectionArgs = selectionArgs;
    }

    @Override
    public boolean onReady() {
        return queriable != null;
    }

    @Override
    public Cursor onExecute() {
        return queriable.query(selectionArgs);
    }

    @Override
    public void onPostExecute(Cursor cursor) {
        super.onPostExecute(cursor);

        if (cursor != null) {
            cursor.close();
        }
    }
}
