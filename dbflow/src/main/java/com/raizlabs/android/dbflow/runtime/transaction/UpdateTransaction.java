package com.raizlabs.android.dbflow.runtime.transaction;

import com.raizlabs.android.dbflow.runtime.DBTransactionInfo;
import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.ConditionGroup;
import com.raizlabs.android.dbflow.sql.language.Update;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: Runs an {@link com.raizlabs.android.dbflow.sql.language.Update} on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}.
 */
public class UpdateTransaction<ModelClass extends Model> extends QueryTransaction<ModelClass> {

    /**
     * Constructs this transaction with WHERE {@link com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder}
     * with a set of {@link Condition} to set on the Update.
     *
     * @param dbTransactionInfo The information about this transaction
     * @param table             The table to update.
     * @param conditionGroup    The set of WHERE conditions to use.
     * @param conditions        The list of SET conditions to use.
     */
    public UpdateTransaction(DBTransactionInfo dbTransactionInfo,
                             Class<ModelClass> table, ConditionGroup conditionGroup, Condition... conditions) {
        super(dbTransactionInfo,
                new Update<>(table).set(conditions).where(conditions), null);
    }

    /**
     * Constructs this UPDATE query with both a WHERE and SET {@link com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder}
     *
     * @param dbTransactionInfo     The information about this transaction
     * @param whereConditionBuilder The set of WHERE conditions to use.
     * @param setConditionBuilder   The set of SET conditions to use.
     */
    public UpdateTransaction(DBTransactionInfo dbTransactionInfo, Class<ModelClass> table,
                             ConditionGroup whereConditionGroup, ConditionGroup setConditionGroup) {
        super(dbTransactionInfo, new Update<>(table).set(setConditionGroup).where(whereConditionGroup));
    }
}
