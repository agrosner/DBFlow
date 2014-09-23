package com.grosner.dbflow.runtime.transaction;

import com.grosner.dbflow.runtime.DBTransactionInfo;
import com.grosner.dbflow.sql.Where;
import com.grosner.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class QueryTransaction<ModelClass extends Model> extends BaseTransaction<Void> {

    private Where<ModelClass> mWhere;

    public QueryTransaction(DBTransactionInfo dbTransactionInfo, Where<ModelClass> where) {
        super(dbTransactionInfo);
        mWhere = where;
    }

    @Override
    public boolean onReady() {
        return mWhere!=null;
    }

    @Override
    public Void onExecute() {
        mWhere.query();
        return null;
    }
}
