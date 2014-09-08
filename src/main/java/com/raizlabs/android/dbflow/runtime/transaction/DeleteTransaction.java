package com.raizlabs.android.dbflow.runtime.transaction;

import com.raizlabs.android.dbflow.runtime.DBTransactionInfo;
import com.raizlabs.android.dbflow.sql.Delete;
import com.raizlabs.android.dbflow.sql.From;
import com.raizlabs.android.dbflow.sql.builder.AbstractWhereQueryBuilder;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class DeleteTransaction<ModelClass extends Model> extends BaseTransaction<Void> {


    private From<ModelClass> mFrom;

    public DeleteTransaction(DBTransactionInfo dbTransactionInfo, Class<ModelClass> table) {
        super(dbTransactionInfo);
        mFrom = new Delete().from(table);
    }

    public DeleteTransaction(DBTransactionInfo dbTransactionInfo, AbstractWhereQueryBuilder<ModelClass> whereArgs,
                             Class<ModelClass> table, String... args) {
        super(dbTransactionInfo);
        mFrom = new Delete().from(table).where(whereArgs, args);
    }

    @Override
    public Void onExecute() {
        mFrom.query();
        return null;
    }

}
