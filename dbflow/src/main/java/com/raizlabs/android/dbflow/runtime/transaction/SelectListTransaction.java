package com.raizlabs.android.dbflow.runtime.transaction;

import com.raizlabs.android.dbflow.runtime.DBTransactionInfo;
import com.raizlabs.android.dbflow.sql.language.ConditionGroup;
import com.raizlabs.android.dbflow.sql.language.SQLCondition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.List;

/**
 * Description: Runs a fetch on the {@link com.raizlabs.android.dbflow.runtime.DBTransactionQueue}
 */
public class SelectListTransaction<ModelClass extends Model> extends BaseResultTransaction<List<ModelClass>> {

    private final ModelQueriable<ModelClass> modelQueriable;
    private final String[] selectionArgs;

    /**
     * Creates an instance of this class
     *
     * @param transactionListener The transaction listener.
     * @param tableClass          The table to select from.
     * @param whereConditions     The conditions to use in a SELECT query.
     */
    public SelectListTransaction(TransactionListener<List<ModelClass>> transactionListener,
                                 Class<ModelClass> tableClass, SQLCondition... whereConditions) {
        this(new Select().from(tableClass).where(whereConditions), transactionListener);
    }

    /**
     * Creates this class with a {@link com.raizlabs.android.dbflow.sql.language.From}
     *
     * @param modelQueriable      The model queriable that defines how to fetch models from the DB.
     * @param transactionListener The result that returns from this query.
     * @param selectionArgs       You may include ?s in selection, which will be replaced by the values
     *                            from selectionArgs, in order that they appear in the selection. The
     *                            values will be bound as Strings.
     */
    public SelectListTransaction(ModelQueriable<ModelClass> modelQueriable, TransactionListener<List<ModelClass>> transactionListener, String... selectionArgs) {
        super(DBTransactionInfo.createFetch(), transactionListener);
        this.modelQueriable = modelQueriable;
        this.selectionArgs = selectionArgs;
    }

    /**
     * Creates an instance of this class
     *
     * @param transactionListener The result that returns from this query.
     * @param conditionGroup      The query builder used to SELECT.
     * @param properties          The columns to select.
     */
    public SelectListTransaction(TransactionListener<List<ModelClass>> transactionListener,
                                 Class<ModelClass> table, ConditionGroup conditionGroup, IProperty... properties) {
        this(new Select(properties).from(table).where(conditionGroup), transactionListener);
    }

    /**
     * Creates an instance of this class. Selects all from the specified table.
     *
     * @param transactionListener The transaction listener.
     * @param table               The table to select from
     * @param columns             The columns to project the selection on.
     */
    public SelectListTransaction(TransactionListener<List<ModelClass>> transactionListener,
                                 Class<ModelClass> table, IProperty... columns) {
        this(new Select(columns).from(table), transactionListener);
    }

    @Override
    public boolean onReady() {
        return modelQueriable != null;
    }

    @Override
    public List<ModelClass> onExecute() {
        return modelQueriable.queryList(selectionArgs);
    }

}
