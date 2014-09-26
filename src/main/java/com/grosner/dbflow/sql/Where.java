package com.grosner.dbflow.sql;

import android.database.Cursor;
import android.database.DatabaseUtils;

import com.grosner.dbflow.config.FlowLog;
import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.runtime.DBTransactionInfo;
import com.grosner.dbflow.runtime.TransactionManager;
import com.grosner.dbflow.runtime.transaction.QueryTransaction;
import com.grosner.dbflow.runtime.transaction.ResultReceiver;
import com.grosner.dbflow.sql.builder.Condition;
import com.grosner.dbflow.sql.builder.ConditionQueryBuilder;
import com.grosner.dbflow.sql.builder.QueryBuilder;
import com.grosner.dbflow.structure.Model;

import java.util.List;
import java.util.Map;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Defines the SQL WHERE statement of the query.
 */
public class Where<ModelClass extends Model> implements Query {

    /**
     * The first chunk of the SQL statement before this query.
     */
    private final From<ModelClass> mFrom;

    /**
     * Helps to build the where statement easily
     */
    private ConditionQueryBuilder<ModelClass> mConditionQueryBuilder;

    /**
     * The SQL GROUP BY method
     */
    private String mGroupBy;

    /**
     * The SQL HAVING
     */
    private ConditionQueryBuilder<ModelClass> mHaving;

    /**
     * The SQL ORDER BY
     */
    private String mOrderBy;

    /**
     * The SQL LIMIT
     */
    private String mLimit;

    /**
     * The SQL OFFSET
     */
    private String mOffset;

    /**
     * The database manager we run this query on
     */
    private final FlowManager mManager;

    /**
     * Constructs this class with a SELECT * on the manager and {@link com.grosner.dbflow.sql.builder.ConditionQueryBuilder}
     *
     * @param flowManager
     * @param conditionQueryBuilder
     * @param <ModelClass>
     * @return
     */
    public static <ModelClass extends Model> Where<ModelClass> with(FlowManager flowManager,
                                                                    ConditionQueryBuilder<ModelClass> conditionQueryBuilder) {
        return new Select(flowManager).from(conditionQueryBuilder.getTableClass()).where(conditionQueryBuilder);
    }

    /**
     * Constructs this class with the specified {@link com.grosner.dbflow.config.FlowManager}
     * and {@link com.grosner.dbflow.sql.From} chunk
     *
     * @param manager The database manager
     * @param from    The FROM statement chunk
     */
    public Where(FlowManager manager, From<ModelClass> from) {
        mManager = manager;
        mFrom = from;
        mConditionQueryBuilder = new ConditionQueryBuilder<ModelClass>(mManager, mFrom.getTable());
        mHaving = new ConditionQueryBuilder<ModelClass>(mManager, mFrom.getTable());
    }

    protected void checkSelect(String methodName) {
        if (!(mFrom.getQueryBuilderBase() instanceof Select)) {
            throw new IllegalArgumentException("Please use " + methodName + "(). The beginning is not a Select");
        }
    }

    /**
     * Defines the full SQL clause for the WHERE statement
     *
     * @param whereClause The SQL after WHERE . ex: columnName = "name" AND ID = 0
     * @return
     */
    public Where<ModelClass> whereClause(String whereClause) {
        mConditionQueryBuilder.append(whereClause);
        return this;
    }

    /**
     * Defines the {@link com.grosner.dbflow.sql.builder.ConditionQueryBuilder} that will build this SQL statement
     *
     * @param conditionQueryBuilder Helps build the SQL after WHERE
     * @return
     */
    public Where<ModelClass> whereQuery(ConditionQueryBuilder<ModelClass> conditionQueryBuilder) {
        mConditionQueryBuilder = conditionQueryBuilder;
        return this;
    }

    /**
     * Adds a param to the WHERE clause with the "=" operator
     *
     * @param columnName The name of column
     * @param value      the value of column
     * @return
     */
    public Where<ModelClass> and(String columnName, Object value) {
        mConditionQueryBuilder.param(columnName, value);
        return this;
    }

    /**
     * Adds a param to the WHERE clause with a custom operator.
     *
     * @param columnName The name of column
     * @param operator   The operator to use. Ex: "=", "<", etc.
     * @param value      The value of the column
     * @return
     */
    public Where<ModelClass> and(String columnName, String operator, Object value) {
        mConditionQueryBuilder.param(columnName, operator, value);
        return this;
    }

    /**
     * Adds a param to the WHERE clause with the custom {@link com.grosner.dbflow.sql.builder.Condition}
     *
     * @param condition The {@link com.grosner.dbflow.sql.builder.Condition} to use
     * @return
     */
    public Where<ModelClass> and(Condition condition) {
        mConditionQueryBuilder.param(condition);
        return this;
    }

    /**
     * Adds a bunch of {@link com.grosner.dbflow.sql.builder.Condition} to this builder.
     *
     * @param conditionMap The map of {@link com.grosner.dbflow.sql.builder.Condition}
     * @return
     */
    public Where<ModelClass> andThese(Map<String, Condition> conditionMap) {
        mConditionQueryBuilder.params(conditionMap);
        return this;
    }

    /**
     * Adds a bunch of {@link com.grosner.dbflow.sql.builder.Condition} to this builder.
     *
     * @param conditions The array of {@link com.grosner.dbflow.sql.builder.Condition}
     * @return
     */
    public Where<ModelClass> andThese(Condition...conditions) {
        mConditionQueryBuilder.params(conditions);
        return this;
    }

    /**
     * Adds primary key params to the where. They must be in order that the primary keys are defined.
     *
     * @param values the values of the primary keys from the table. Must be in order of the primary key declaration.
     * @return
     */
    public Where<ModelClass> andPrimaryValues(Object... values) {
        mConditionQueryBuilder.primaryParams(values);
        return this;
    }

    /**
     * Defines a SQL GROUP BY statement without the GROUP BY.
     *
     * @param groupBy
     * @return
     */
    public Where<ModelClass> groupBy(QueryBuilder groupBy) {
        mGroupBy = groupBy.getQuery();
        return this;
    }

    /**
     * Defines a SQL HAVING statement without the HAVING.
     *
     * @param having
     * @return
     */
    public Where<ModelClass> having(Condition...conditions) {
        mHaving.params(conditions);
        return this;
    }

    /**
     * Defines a SQL ORDER BY statement without the ORDER BY.
     *
     * @param orderBy
     * @return
     */
    public Where<ModelClass> orderBy(QueryBuilder orderBy) {
        mOrderBy = orderBy.getQuery();
        return this;
    }

    /**
     * Defines a SQL LIMIT statement without the LIMIT.
     *
     * @param limit
     * @return
     */
    public Where<ModelClass> limit(Object limit) {
        mLimit = String.valueOf(limit);
        return this;
    }

    /**
     * Defines a SQL OFFSET statement without the OFFSET.
     *
     * @param offset
     * @return
     */
    public Where<ModelClass> offset(Object offset) {
        mOffset = String.valueOf(offset);
        return this;
    }

    /**
     * Executes a SQL statement that retrieves the count of results in the DB.
     *
     * @return The number of rows this query returns
     */
    public long count() {
        return DatabaseUtils.longForQuery(mManager.getWritableDatabase(), getQuery(), null);
    }

    /**
     * Run this query and returns the {@link android.database.Cursor} for it
     * @return the Sqlite {@link android.database.Cursor} from this query
     */
    public Cursor query() {
        // Query the sql here
        return mManager.getWritableDatabase().rawQuery(getQuery(), null);
    }

    /**
     * Queries for all of the results this statement returns from a DB cursor in the form of the {@link ModelClass}
     *
     * @return All of the entries in the DB converted into {@link ModelClass}
     */
    public List<ModelClass> queryList() {
        checkSelect("query");
        return SqlUtils.queryList(mManager, mFrom.getTable(), getQuery());
    }

    /**
     * Queries and returns only the first {@link ModelClass} result from the DB.
     *
     * @return The first result of this query. Note: this query may return more than one from the DB.
     */
    public ModelClass querySingle() {
        checkSelect("query");
        return SqlUtils.querySingle(mManager, mFrom.getTable(), getQuery());
    }

    /**
     * Will run this query on the {@link com.grosner.dbflow.runtime.DBTransactionQueue}
     *
     * @param transactionInfo    The information on how to prioritize the transaction
     * @param transactionManager The transaction manager to add the query to
     */
    public void transact(DBTransactionInfo transactionInfo, TransactionManager transactionManager) {
        transactionManager.addTransaction(new QueryTransaction<ModelClass>(transactionInfo, this));
    }

    /**
     * Will run this query on the {@link com.grosner.dbflow.runtime.DBTransactionQueue} with the shared
     * {@link com.grosner.dbflow.runtime.TransactionManager}
     *
     * @param transactionInfo The information on how to prioritize the transaction
     */
    public void transact(DBTransactionInfo transactionInfo) {
        transact(transactionInfo, TransactionManager.getInstance());
    }

    /**
     * Puts this query onto the {@link com.grosner.dbflow.runtime.DBTransactionQueue} and will return a list of
     * {@link ModelClass} on the UI thread.
     *
     * @param transactionManager The transaction manager to add the query to
     * @param listResultReceiver The result of this transaction
     */
    public void transactList(TransactionManager transactionManager, ResultReceiver<List<ModelClass>> listResultReceiver) {
        checkSelect("transact");
        transactionManager.fetchFromTable(this, listResultReceiver);
    }

    /**
     * Puts this query onto the {@link com.grosner.dbflow.runtime.DBTransactionQueue} and will return a list of
     * {@link ModelClass} on the UI thread with the shared {@link com.grosner.dbflow.runtime.TransactionManager}.
     *
     * @param listResultReceiver The result of this transaction
     */
    public void transactList(ResultReceiver<List<ModelClass>> listResultReceiver) {
        transactList(TransactionManager.getInstance(), listResultReceiver);
    }

    /**
     * Puts this query onto the {@link com.grosner.dbflow.runtime.DBTransactionQueue} and will return
     * a single item on the UI thread.
     *
     * @param transactionManager The transaction manager to add the query to
     * @param resultReceiver     The result of this transaction
     */
    public void transactSingleModel(TransactionManager transactionManager, ResultReceiver<ModelClass> resultReceiver) {
        checkSelect("transact");
        transactionManager.fetchModel(this, resultReceiver);
    }

    /**
     * Puts this query onto the {@link com.grosner.dbflow.runtime.DBTransactionQueue} and will return
     * a single item on the UI thread with the shared {@link com.grosner.dbflow.runtime.TransactionManager}.
     *
     * @param resultReceiver The result of this transaction
     */
    public void transactSingleModel(ResultReceiver<ModelClass> resultReceiver) {
        transactSingleModel(TransactionManager.getInstance(), resultReceiver);
    }

    /**
     * Returns whether the DB {@link android.database.Cursor} returns with a count of at least 1
     *
     * @return if {@link android.database.Cursor}.count > 0
     */
    public boolean hasData() {
        checkSelect("query");
        return SqlUtils.hasData(mManager, mFrom.getTable(), getQuery());
    }

    /**
     * Returns the table this query points to
     *
     * @return
     */
    public Class<ModelClass> getTable() {
        return mFrom.getTable();
    }

    @Override
    public String getQuery() {
        String fromQuery = mFrom.getQuery();
        QueryBuilder queryBuilder = new QueryBuilder().append(fromQuery).appendSpace();

        queryBuilder.appendQualifier("WHERE", mConditionQueryBuilder.getQuery())
                .appendQualifier("GROUP BY", mGroupBy)
                .appendQualifier("HAVING", mHaving.getQuery())
                .appendQualifier("ORDER BY", mOrderBy)
                .appendQualifier("LIMIT", mLimit)
                .appendQualifier("OFFSET", mOffset);

        // Don't wast time building the string
        // unless we're going to log it.
        if (FlowLog.isEnabled(FlowLog.Level.V)) {
            FlowLog.log(FlowLog.Level.V, queryBuilder.getQuery());
        }

        return queryBuilder.getQuery();
    }
}
