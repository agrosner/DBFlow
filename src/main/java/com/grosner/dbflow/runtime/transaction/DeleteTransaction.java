package com.grosner.dbflow.runtime.transaction;

import com.grosner.dbflow.config.FlowManager;
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

    /**
     * Constructs this transaction with a delete with an empty "where" clause
     *
     * @param flowManager       The manager that corresponds to a database
     * @param dbTransactionInfo The information about this transaction
     * @param table             The model table that we act on
     */
    public DeleteTransaction(FlowManager flowManager, DBTransactionInfo dbTransactionInfo, Class<ModelClass> table) {
        super(dbTransactionInfo);
        Where = new Delete(flowManager).from(table).where();
    }

    /**
     * Constructs this transaction with a delete with the specified where args
     *
     * @param flowManager       The manager that corresponds to a database
     * @param dbTransactionInfo The information about this transaction
     * @param table             The model table that we act on
     * @param whereArgs         The where statement that we will use
     */
    public DeleteTransaction(FlowManager flowManager, DBTransactionInfo dbTransactionInfo, Class<ModelClass> table,
                             WhereQueryBuilder<ModelClass> whereArgs) {
        super(dbTransactionInfo);
        Where = new Delete(flowManager).from(table).where().whereQuery(whereArgs);
    }

    @Override
    public Void onExecute() {
        Where.query();
        return null;
    }

}
