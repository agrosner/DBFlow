package com.raizlabs.android.dbflow.runtime.transaction;

import com.raizlabs.android.dbflow.runtime.DBTransactionInfo;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: Runs a delete command on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
 */
public class DeleteTransaction<ModelClass extends Model> extends QueryTransaction<ModelClass> {

    /**
     * Constructs this transaction with a delete with an empty "where" clause
     *
     * @param dbTransactionInfo The information about this transaction
     * @param table             The model table that we act on
     */
    public DeleteTransaction(DBTransactionInfo dbTransactionInfo,
                             Class<ModelClass> table, Condition... conditions) {
        super(dbTransactionInfo, new Delete().from(table).where(conditions));
    }

    /**
     * Constructs this transaction with a delete with the specified where args
     *
     * @param dbTransactionInfo     The information about this transaction
     * @param conditionQueryBuilder The where statement that we will use
     */
    public DeleteTransaction(DBTransactionInfo dbTransactionInfo,
                             ConditionQueryBuilder<ModelClass> conditionQueryBuilder) {
        super(dbTransactionInfo, new Delete().from(conditionQueryBuilder.getTableClass()).where(conditionQueryBuilder));
    }
}
