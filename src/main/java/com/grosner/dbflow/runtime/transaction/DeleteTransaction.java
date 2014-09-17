package com.grosner.dbflow.runtime.transaction;

import com.grosner.dbflow.runtime.DBTransactionInfo;
import com.grosner.dbflow.sql.Delete;
import com.grosner.dbflow.sql.Where;
import com.grosner.dbflow.sql.builder.WhereQueryBuilder;
import com.grosner.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Runs a delete command on the {@link com.grosner.dbflow.runtime.DBTransactionQueue}
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
