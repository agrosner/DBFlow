package com.raizlabs.android.dbflow.runtime.transaction;

import com.raizlabs.android.dbflow.runtime.DBTransactionInfo;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.List;

/**
 * Description: Runs a fetch on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
 */
public class SelectListTransaction<ModelClass extends Model> extends BaseResultTransaction<List<ModelClass>> {

    private ModelQueriable<ModelClass> mModelQueriable;

    /**
     * Creates an instance of this class
     *
     * @param transactionListener The transaction listener.
     * @param tableClass          The table to select from.
     * @param whereConditions     The conditions to use in a SELECT query.
     */
    public SelectListTransaction(TransactionListener<List<ModelClass>> transactionListener,
                                 Class<ModelClass> tableClass, Condition... whereConditions) {
        this(new Select().from(tableClass).where(whereConditions), transactionListener);
    }

    /**
     * Creates this class with a {@link com.raizlabs.android.dbflow.sql.language.From}
     *
     * @param modelQueriable      The model queriable that defines how to fetch models from the DB.
     * @param transactionListener The result that returns from this query.
     */
    public SelectListTransaction(ModelQueriable<ModelClass> modelQueriable, TransactionListener<List<ModelClass>> transactionListener) {
        super(DBTransactionInfo.createFetch(), transactionListener);
        mModelQueriable = modelQueriable;
    }

    /**
     * Creates an instance of this class
     *
     * @param transactionListener        The result that returns from this query.
     * @param whereConditionQueryBuilder The query builder used to SELECT.
     * @param columns                    The columns to select.
     */
    public SelectListTransaction(TransactionListener<List<ModelClass>> transactionListener,
                                 ConditionQueryBuilder<ModelClass> whereConditionQueryBuilder, String... columns) {
        this(new Select(columns).from(whereConditionQueryBuilder.getTableClass()).where(whereConditionQueryBuilder), transactionListener);
    }

    /**
     * Creates an instance of this class. Selects all from the specified table.
     *
     * @param transactionListener The transaction listener.
     * @param table               The table to select from
     * @param columns             The columns to project the selection on.
     */
    public SelectListTransaction(TransactionListener<List<ModelClass>> transactionListener,
                                 Class<ModelClass> table, String... columns) {
        this(new Select(columns).from(table), transactionListener);
    }

    @Override
    public boolean onReady() {
        return mModelQueriable != null;
    }

    @Override
    public List<ModelClass> onExecute() {
        return mModelQueriable.queryList();
    }

}
