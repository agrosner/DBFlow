package com.grosner.dbflow.runtime.transaction;

import android.database.Cursor;

import com.grosner.dbflow.runtime.DBTransactionInfo;
import com.grosner.dbflow.sql.Queriable;
import com.grosner.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Runs a DB query in the BG on the {@link com.grosner.dbflow.runtime.DBTransactionQueue}. It supplies
 * the cursor returned from this query and will automatically closes the cursor to prevent database leaks.
 */
public class QueryTransaction<ModelClass extends Model> extends BaseResultTransaction<Cursor> {

    private Queriable<ModelClass> mQueriable;

    public QueryTransaction(DBTransactionInfo dbTransactionInfo, Queriable<ModelClass> queriable) {
        this(dbTransactionInfo, queriable, null);
    }

    public QueryTransaction(DBTransactionInfo dbTransactionInfo, Queriable<ModelClass> queriable, ResultReceiver<Cursor> cursorResultReceiver) {
        super(dbTransactionInfo, cursorResultReceiver);
        mQueriable = queriable;
    }

    @Override
    public boolean onReady() {
        return mQueriable != null;
    }

    @Override
    public Cursor onExecute() {
        return mQueriable.query();
    }

    @Override
    public void onPostExecute(Cursor cursor) {
        super.onPostExecute(cursor);

        if (cursor != null) {
            cursor.close();
        }
    }
}
