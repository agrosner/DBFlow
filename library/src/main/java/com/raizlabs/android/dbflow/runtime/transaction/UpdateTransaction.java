package com.raizlabs.android.dbflow.runtime.transaction;

import com.raizlabs.android.dbflow.runtime.DBTransactionInfo;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder;
import com.raizlabs.android.dbflow.sql.language.Update;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class UpdateTransaction<ModelClass extends Model> extends QueryTransaction<ModelClass> {

    public UpdateTransaction(DBTransactionInfo dbTransactionInfo,
                             ConditionQueryBuilder<ModelClass> whereConditionBuilder, Condition... conditions) {
        super(dbTransactionInfo, new Update().table(whereConditionBuilder.getTableClass()).set(conditions).where(whereConditionBuilder), null);
    }

    public UpdateTransaction(DBTransactionInfo dbTransactionInfo,
                             ConditionQueryBuilder<ModelClass> whereConditionBuilder, ConditionQueryBuilder<ModelClass> setConditionBuilder) {
        super(dbTransactionInfo, new Update().table(whereConditionBuilder.getTableClass())
                .set(setConditionBuilder).where(whereConditionBuilder));
    }
}
