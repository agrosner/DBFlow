package com.raizlabs.android.dbflow.runtime.transaction;

import com.raizlabs.android.dbflow.runtime.DBTransactionInfo;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Where;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Runs a fetch on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
 */
public class SelectListTransaction<ModelClass extends Model> extends BaseResultTransaction<List<ModelClass>> {

    private Where<ModelClass> mWhere;

    /**
     * Creates an instance of this class
     *
     * @param flowManager         The database manager to use
     * @param transactionListener The result that returns from this query
     * @param tableClass          The table to select from
     * @param whereConditions     The conditions to use in the SELECT query
     */
    public SelectListTransaction(TransactionListener<List<ModelClass>> transactionListener,
                                 Class<ModelClass> tableClass, Condition... whereConditions) {
        this(new Select().from(tableClass).where(whereConditions), transactionListener);
    }

    /**
     * Creates this class with a {@link com.raizlabs.android.dbflow.sql.language.From}
     *
     * @param where               The completed Sql Statement we will use to fetch the models
     * @param transactionListener The result that returns from this query
     */
    public SelectListTransaction(Where<ModelClass> where, TransactionListener<List<ModelClass>> transactionListener) {
        super(DBTransactionInfo.createFetch(), transactionListener);
        mWhere = where;
    }

    /**
     * Creates an instance of this class
     *
     * @param transactionListener        The result that returns from this query
     * @param whereConditionQueryBuilder The query builder used to SELECT
     * @param columns                    The columns to select
     */
    public SelectListTransaction(TransactionListener<List<ModelClass>> transactionListener,
                                 ConditionQueryBuilder<ModelClass> whereConditionQueryBuilder, String... columns) {
        this(new Select(columns).from(whereConditionQueryBuilder.getTableClass()).where(whereConditionQueryBuilder), transactionListener);
    }

    /**
     * Creates an instance of this class
     *
     * @param transactionListener The result that returns from this query
     * @param table               The table to select from
     * @param columns             The columns to select
     */
    public SelectListTransaction(TransactionListener<List<ModelClass>> transactionListener,
                                 Class<ModelClass> table, String... columns) {
        this(new Select(columns).from(table).where(), transactionListener);
    }

    @Override
    public boolean onReady() {
        return mWhere != null;
    }

    @Override
    public List<ModelClass> onExecute() {
        return mWhere.queryList();
    }

}
