package com.raizlabs.android.dbflow.runtime.transaction;

import com.raizlabs.android.dbflow.runtime.DBTransactionInfo;
import com.raizlabs.android.dbflow.sql.Delete;
import com.raizlabs.android.dbflow.sql.Where;
import com.raizlabs.android.dbflow.sql.builder.WhereQueryBuilder;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Runs a delete command on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
 */
public class DeleteTransaction<ModelClass extends Model> extends BaseTransaction<Void> {


    private Where<ModelClass> Where;

    public DeleteTransaction(DBTransactionInfo dbTransactionInfo, Class<ModelClass> table) {
        super(dbTransactionInfo);
        Where = new Delete().from(table).where();
    }

    public DeleteTransaction(DBTransactionInfo dbTransactionInfo, Class<ModelClass> table,
                             WhereQueryBuilder<ModelClass> whereArgs) {
        super(dbTransactionInfo);
        Where = new Delete().from(table).where().whereQuery(whereArgs);
    }

    @Override
    public Void onExecute() {
        Where.query();
        return null;
    }

}
