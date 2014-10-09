package com.grosner.dbflow.runtime.transaction;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.runtime.DBTransactionInfo;
import com.grosner.dbflow.sql.Update;
import com.grosner.dbflow.sql.builder.Condition;
import com.grosner.dbflow.sql.builder.ConditionQueryBuilder;
import com.grosner.dbflow.structure.Model;

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
