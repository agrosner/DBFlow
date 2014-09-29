package com.grosner.dbflow.runtime.transaction;

import android.database.Cursor;

import com.grosner.dbflow.runtime.DBTransactionInfo;
import com.grosner.dbflow.sql.Where;
import com.grosner.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Runs a DB query in the BG on the {@link com.grosner.dbflow.runtime.DBTransactionQueue}. It supplies
 * the cursor returned from this query and will automatically closes the cursor to prevent database leaks.
 */
public class QueryTransaction<ModelClass extends Model> extends BaseResultTransaction<Cursor> {

    private Where<ModelClass> mWhere;

    public QueryTransaction(DBTransactionInfo dbTransactionInfo, Where<ModelClass> where, ResultReceiver<Cursor> cursorResultReceiver) {
        super(dbTransactionInfo, cursorResultReceiver);
        mWhere = where;
    }

    public QueryTransaction(DBTransactionInfo dbTransactionInfo, Where<ModelClass> where) {
        this(dbTransactionInfo, where, null);
    }

    @Override
    public boolean onReady() {
        return mWhere != null;
    }

    @Override
    public Cursor onExecute() {
        return mWhere.query();
    }

    @Override
    public void onPostExecute(Cursor cursor) {
        super.onPostExecute(cursor);

        if(cursor != null) {
            cursor.close();
        }
    }
}
