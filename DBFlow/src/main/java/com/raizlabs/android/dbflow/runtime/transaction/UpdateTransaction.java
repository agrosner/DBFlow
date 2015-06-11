package com.raizlabs.android.dbflow.runtime.transaction;

import com.raizlabs.android.dbflow.runtime.DBTransactionInfo;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder;
import com.raizlabs.android.dbflow.sql.language.Update;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: Runs an {@link com.raizlabs.android.dbflow.sql.language.Update} on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}.
 */
public class UpdateTransaction<ModelClass extends Model> extends QueryTransaction<ModelClass> {

    /**
     * Constructs this transaction with WHERE {@link com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder}
     * with a set of {@link com.raizlabs.android.dbflow.sql.builder.Condition} to set on the Update.
     *
     * @param dbTransactionInfo     The information about this transaction
     * @param whereConditionBuilder The set of WHERE conditions to use.
     * @param conditions            The list of SET conditions to use.
     */
    public UpdateTransaction(DBTransactionInfo dbTransactionInfo,
                             ConditionQueryBuilder<ModelClass> whereConditionBuilder, Condition... conditions) {
        super(dbTransactionInfo,
              new Update<>(whereConditionBuilder.getTableClass()).set(conditions).where(whereConditionBuilder), null);
    }

    /**
     * Constructs this UPDATE query with both a WHERE and SET {@link com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder}
     *
     * @param dbTransactionInfo     The information about this transaction
     * @param whereConditionBuilder The set of WHERE conditions to use.
     * @param setConditionBuilder   The set of SET conditions to use.
     */
    public UpdateTransaction(DBTransactionInfo dbTransactionInfo,
                             ConditionQueryBuilder<ModelClass> whereConditionBuilder,
                             ConditionQueryBuilder<ModelClass> setConditionBuilder) {
        super(dbTransactionInfo, new Update<>(whereConditionBuilder.getTableClass())
                .set(setConditionBuilder).where(whereConditionBuilder));
    }
}
