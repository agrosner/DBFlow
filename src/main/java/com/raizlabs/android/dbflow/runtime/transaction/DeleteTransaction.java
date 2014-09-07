package com.raizlabs.android.dbflow.runtime.transaction;

import com.raizlabs.android.dbflow.runtime.DBTransactionInfo;
import com.raizlabs.android.dbflow.sql.Delete;
import com.raizlabs.android.dbflow.sql.From;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class DeleteTransaction<ModelClass extends Model> extends BaseTransaction<Void> {


    private From mFrom;

    public DeleteTransaction(DBTransactionInfo dbTransactionInfo, Class<ModelClass> table) {
        super(dbTransactionInfo);
        mFrom = new Delete().from(table);
    }

    public DeleteTransaction(DBTransactionInfo dbTransactionInfo, DeleteWhereArgs whereArgs, Class<ModelClass> table) {
        super(dbTransactionInfo);
        mFrom = new Delete().from(table).where(whereArgs.mWhere, whereArgs.mArgs);
    }

    @Override
    public Void onExecute() {
        mFrom.query();
        return null;
    }

    /**
     * Defines a simple class for holding the "where" statement and the arguments associated with it.
     */
    public static class DeleteWhereArgs {

        final String mWhere;

        final Object[] mArgs;

        public DeleteWhereArgs(String where, Object...args) {
            mWhere = where;
            mArgs = args;
        }
    }

}
