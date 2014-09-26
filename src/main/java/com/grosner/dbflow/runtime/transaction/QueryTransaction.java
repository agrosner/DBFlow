package com.grosner.dbflow.runtime.transaction;

import android.database.Cursor;

import com.grosner.dbflow.runtime.DBTransactionInfo;
import com.grosner.dbflow.sql.Where;
import com.grosner.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
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
}
