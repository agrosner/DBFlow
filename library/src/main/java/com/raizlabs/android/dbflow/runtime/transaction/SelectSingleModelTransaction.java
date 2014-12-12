package com.raizlabs.android.dbflow.runtime.transaction;

import com.raizlabs.android.dbflow.runtime.DBTransactionInfo;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Where;
import com.raizlabs.android.dbflow.structure.Model;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Runs a fetch on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}, returning only the first item.
 */
public class SelectSingleModelTransaction<ModelClass extends Model> extends BaseResultTransaction<ModelClass> {

    private Where<ModelClass> mWhere;

    /**
     * Creates an instance of this class
     *
     * @param resultReceiver  The result that returns from this query
     * @param tableClass      The table to select from
     * @param whereConditions The conditions to use in the SELECT query
     */
    public SelectSingleModelTransaction(Class<ModelClass> tableClass,
                                        ResultReceiver<ModelClass> resultReceiver, Condition... whereConditions) {
        this(new Select().from(tableClass).where(whereConditions), resultReceiver);
    }

    /**
     * Creates this class with a {@link com.raizlabs.android.dbflow.sql.language.From}
     *
     * @param where          The completed Sql Statement we will use to fetch the models
     * @param resultReceiver
     */
    public SelectSingleModelTransaction(Where<ModelClass> where, ResultReceiver<ModelClass> resultReceiver) {
        super(DBTransactionInfo.createFetch(), resultReceiver);
        mWhere = where;
    }

    /**
     * Creates an instance of this class
     *
     * @param resultReceiver             The result that returns from this query
     * @param whereConditionQueryBuilder The query builder used to SELECT
     * @param columns                    The columns to select
     */
    public SelectSingleModelTransaction(ResultReceiver<ModelClass> resultReceiver,
                                        ConditionQueryBuilder<ModelClass> whereConditionQueryBuilder, String... columns) {
        this(new Select(columns).from(whereConditionQueryBuilder.getTableClass()).where(whereConditionQueryBuilder), resultReceiver);
    }

    @Override
    public ModelClass onExecute() {
        return mWhere.querySingle();
    }
}
