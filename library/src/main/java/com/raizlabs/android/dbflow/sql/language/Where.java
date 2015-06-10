package com.raizlabs.android.dbflow.sql.language;

import android.database.Cursor;
import android.database.DatabaseUtils;

import com.raizlabs.android.dbflow.SQLiteCompatibilityUtils;
import com.raizlabs.android.dbflow.config.BaseDatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.SqlUtils;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder;
import com.raizlabs.android.dbflow.sql.builder.SQLCondition;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.List;

/**
 * Description: Defines the SQL WHERE statement of the query.
 */
public class Where<ModelClass extends Model> extends BaseModelQueriable<ModelClass>
        implements Query, ModelQueriable<ModelClass> {

    /**
     * The first chunk of the SQL statement before this query.
     */
    private final WhereBase<ModelClass> mWhereBase;
    /**
     * The database manager we run this query on
     */
    private final BaseDatabaseDefinition mManager;
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
     * Marks this WHERE as only using the EXIST keyword.
     */
    private boolean isExistWhere;

    /**
     * Constructs this class with the specified {@link com.raizlabs.android.dbflow.config.FlowManager}
     * and {@link From} chunk
     *
     * @param whereBase The FROM or SET statement chunk
     */
    public Where(WhereBase<ModelClass> whereBase) {
        super(whereBase.getTable());
        mWhereBase = whereBase;
        mManager = FlowManager.getDatabaseForTable(mWhereBase.getTable());
        mConditionQueryBuilder = new ConditionQueryBuilder<>(mWhereBase.getTable());
        mHaving = new ConditionQueryBuilder<>(mWhereBase.getTable());
    }

    /**
     * Defines the full SQL clause for the WHERE statement
     *
     * @param whereClause The SQL after WHERE . ex: columnName = "name" AND ID = 0
     * @param args        The optional arguments for the wher clause.
     * @return
     */
    public Where<ModelClass> whereClause(String whereClause, Object... args) {
        mConditionQueryBuilder.append(whereClause, args);
        return this;
    }

    /**
     * Defines the {@link com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder} that will build this SQL statement
     *
     * @param conditionQueryBuilder Helps build the SQL after WHERE
     * @return
     */
    public Where<ModelClass> whereQuery(ConditionQueryBuilder<ModelClass> conditionQueryBuilder) {
        if (conditionQueryBuilder != null) {
            mConditionQueryBuilder = conditionQueryBuilder;
        }
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
        mConditionQueryBuilder.addCondition(columnName, value);
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
        mConditionQueryBuilder.addCondition(columnName, operator, value);
        return this;
    }

    /**
     * Adds a param to the WHERE clause with the custom {@link com.raizlabs.android.dbflow.sql.builder.SQLCondition}
     *
     * @param condition The {@link com.raizlabs.android.dbflow.sql.builder.SQLCondition} to use
     * @return
     */
    public Where<ModelClass> and(SQLCondition condition) {
        mConditionQueryBuilder.and(condition);
        return this;
    }

    /**
     * Appends an OR with a Condition to the WHERE clause with the specified {@link com.raizlabs.android.dbflow.sql.builder.SQLCondition}
     *
     * @param condition
     * @return
     */
    public Where<ModelClass> or(SQLCondition condition) {
        mConditionQueryBuilder.or(condition);
        return this;
    }

    /**
     * Adds a bunch of {@link com.raizlabs.android.dbflow.sql.builder.Condition} to this builder.
     *
     * @param conditions The list of {@link com.raizlabs.android.dbflow.sql.builder.SQLCondition}
     * @return
     */
    public Where<ModelClass> andThese(List<SQLCondition> conditions) {
        mConditionQueryBuilder.addConditions(conditions);
        return this;
    }

    /**
     * Adds a bunch of {@link com.raizlabs.android.dbflow.sql.builder.SQLCondition} to this builder.
     *
     * @param conditions The array of {@link com.raizlabs.android.dbflow.sql.builder.SQLCondition}
     * @return
     */
    public Where<ModelClass> andThese(SQLCondition... conditions) {
        mConditionQueryBuilder.addConditions(conditions);
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
     * Defines a SQL GROUP BY statement without the GROUP BY.
     *
     * @param columns The columns to groupby
     * @return
     */
    public Where<ModelClass> groupBy(ColumnAlias... columns) {
        mGroupBy = new QueryBuilder().appendArray(columns)
                .getQuery();
        return this;
    }

    /**
     * Defines a SQL GROUP BY statement without the GROUP BY.
     *
     * @param columns The columns to groupby
     * @return
     */
    public Where<ModelClass> groupBy(String... columns) {
        mGroupBy = new QueryBuilder().appendArray(columns)
                .getQuery();
        return this;
    }

    /**
     * Defines a SQL HAVING statement without the HAVING.
     *
     * @param conditions The array of {@link com.raizlabs.android.dbflow.sql.builder.SQLCondition}
     * @return
     */
    public Where<ModelClass> having(SQLCondition... conditions) {
        mHaving.addConditions(conditions);
        return this;
    }

    /**
     * Defines a SQL ORDER BY statement without the ORDER BY.
     *
     * @param ascending If we should be in ascending order
     * @return
     */
    public Where<ModelClass> orderBy(boolean ascending, String... columns) {
        mOrderBy = new QueryBuilder().appendArray(columns)
                .appendSpace()
                .append(ascending ? "ASC" : "DESC")
                .getQuery();
        return this;
    }

    /**
     * Defines a SQL ORDER BY statement without the ORDER BY.
     *
     * @param orderby The orderBy command
     * @return
     */
    public Where<ModelClass> orderBy(String orderby) {
        mOrderBy = orderby;
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
     * Sets this statement to only specify that it EXISTS
     *
     * @return
     */
    public Where<ModelClass> exists(Where where) {
        mConditionQueryBuilder.addCondition(Condition.exists()
                                                    .operation("")
                                                    .value(where));
        return this;
    }

    /**
     * Executes a SQL statement that retrieves the count of results in the DB.
     *
     * @return The number of rows this query returns
     */
    public long count() {
        long count;
        if ((mWhereBase instanceof Set) || mWhereBase.getQueryBuilderBase() instanceof Delete) {
            count = SQLiteCompatibilityUtils.executeUpdateDelete(mManager.getWritableDatabase(), getQuery());
        } else {
            count = DatabaseUtils.longForQuery(mManager.getWritableDatabase(), getQuery(), null);
        }
        return count;
    }

    @Override
    public String getQuery() {
        String fromQuery = mWhereBase.getQuery();
        QueryBuilder queryBuilder = new QueryBuilder().append(fromQuery)
                .appendQualifier("WHERE", mConditionQueryBuilder.getQuery())
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

    /**
     * Run this query and returns the {@link android.database.Cursor} for it
     *
     * @return the Sqlite {@link android.database.Cursor} from this query
     */
    @Override
    public Cursor query() {
        // Query the sql here
        Cursor cursor = null;
        String query = getQuery();
        if (mWhereBase.getQueryBuilderBase() instanceof Select) {
            cursor = mManager.getWritableDatabase()
                    .rawQuery(query, null);
        } else {
            mManager.getWritableDatabase()
                    .execSQL(query);
        }

        return cursor;
    }

    @Override
    public void queryClose() {
        Cursor query = query();
        if (query != null) {
            query.close();
        }
    }

    /**
     * Queries for all of the results this statement returns from a DB cursor in the form of the {@link ModelClass}
     *
     * @return All of the entries in the DB converted into {@link ModelClass}
     */
    @Override
    public List<ModelClass> queryList() {
        checkSelect("query");
        return super.queryList();
    }

    protected void checkSelect(String methodName) {
        if (!(mWhereBase.getQueryBuilderBase() instanceof Select)) {
            throw new IllegalArgumentException("Please use " + methodName + "(). The beginning is not a Select");
        }
    }

    /**
     * Queries and returns only the first {@link ModelClass} result from the DB. Will enforce a limit of 1 item
     * returned from the database.
     *
     * @return The first result of this query. Note: this query may return more than one from the DB.
     */
    @Override
    public ModelClass querySingle() {
        checkSelect("query");
        limit(1);
        return super.querySingle();
    }

    /**
     * Returns whether the DB {@link android.database.Cursor} returns with a count of at least 1
     *
     * @return if {@link android.database.Cursor}.count > 0
     */
    public boolean hasData() {
        checkSelect("query");
        return SqlUtils.hasData(mWhereBase.getTable(), getQuery());
    }
}
