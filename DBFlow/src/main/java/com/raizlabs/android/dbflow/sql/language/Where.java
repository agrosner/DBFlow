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
    private final WhereBase<ModelClass> whereBase;
    /**
     * The database manager we run this query on
     */
    private final BaseDatabaseDefinition databaseDefinition;
    /**
     * Helps to build the where statement easily
     */
    private ConditionQueryBuilder<ModelClass> conditionQueryBuilder;
    /**
     * The SQL GROUP BY method
     */
    private String groupBy;
    /**
     * The SQL HAVING
     */
    private ConditionQueryBuilder<ModelClass> having;
    /**
     * The SQL ORDER BY
     */
    private String orderBy;
    /**
     * The SQL LIMIT
     */
    private String limit;
    /**
     * The SQL OFFSET
     */
    private String offset;

    /**
     * Constructs this class with the specified {@link com.raizlabs.android.dbflow.config.FlowManager}
     * and {@link From} chunk
     *
     * @param whereBase The FROM or SET statement chunk
     */
    public Where(WhereBase<ModelClass> whereBase) {
        super(whereBase.getTable());
        this.whereBase = whereBase;
        databaseDefinition = FlowManager.getDatabaseForTable(this.whereBase.getTable());
        conditionQueryBuilder = new ConditionQueryBuilder<>(this.whereBase.getTable());
        having = new ConditionQueryBuilder<>(this.whereBase.getTable());
    }

    /**
     * Defines the full SQL clause for the WHERE statement
     *
     * @param whereClause The SQL after WHERE . ex: columnName = "name" AND ID = 0
     * @param args        The optional arguments for the wher clause.
     * @return
     */
    public Where<ModelClass> whereClause(String whereClause, Object... args) {
        conditionQueryBuilder.append(whereClause, args);
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
            this.conditionQueryBuilder = conditionQueryBuilder;
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
        conditionQueryBuilder.addCondition(columnName, value);
        return this;
    }

    /**
     * Adds a param to the WHERE clause with a custom operator.
     *
     * @param columnName The name of column
     * @param operator   The operator to use. Ex: "=", "&lt;", etc.
     * @param value      The value of the column
     * @return
     */
    public Where<ModelClass> and(String columnName, String operator, Object value) {
        conditionQueryBuilder.addCondition(columnName, operator, value);
        return this;
    }

    /**
     * Adds a param to the WHERE clause with the custom {@link com.raizlabs.android.dbflow.sql.builder.SQLCondition}
     *
     * @param condition The {@link com.raizlabs.android.dbflow.sql.builder.SQLCondition} to use
     * @return
     */
    public Where<ModelClass> and(SQLCondition condition) {
        conditionQueryBuilder.and(condition);
        return this;
    }

    /**
     * Appends an OR with a Condition to the WHERE clause with the specified {@link com.raizlabs.android.dbflow.sql.builder.SQLCondition}
     *
     * @param condition
     * @return
     */
    public Where<ModelClass> or(SQLCondition condition) {
        conditionQueryBuilder.or(condition);
        return this;
    }

    /**
     * Adds a bunch of {@link com.raizlabs.android.dbflow.sql.builder.Condition} to this builder.
     *
     * @param conditions The list of {@link com.raizlabs.android.dbflow.sql.builder.SQLCondition}
     * @return
     */
    public Where<ModelClass> andThese(List<SQLCondition> conditions) {
        conditionQueryBuilder.addConditions(conditions);
        return this;
    }

    /**
     * Adds a bunch of {@link com.raizlabs.android.dbflow.sql.builder.SQLCondition} to this builder.
     *
     * @param conditions The array of {@link com.raizlabs.android.dbflow.sql.builder.SQLCondition}
     * @return
     */
    public Where<ModelClass> andThese(SQLCondition... conditions) {
        conditionQueryBuilder.addConditions(conditions);
        return this;
    }

    /**
     * Defines a SQL GROUP BY statement without the GROUP BY.
     *
     * @param groupBy
     * @return
     */
    public Where<ModelClass> groupBy(QueryBuilder groupBy) {
        this.groupBy = groupBy.getQuery();
        return this;
    }

    /**
     * Defines a SQL GROUP BY statement without the GROUP BY.
     *
     * @param columns The columns to groupby
     * @return
     */
    public Where<ModelClass> groupBy(ColumnAlias... columns) {
        groupBy = new QueryBuilder().appendArray(columns)
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
        groupBy = new QueryBuilder().appendArray(columns)
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
        having.addConditions(conditions);
        return this;
    }

    /**
     * @param ascending If we should be in ascending order
     * @param columns   the columns to specify.
     * @return This WHERE query.
     */
    public Where<ModelClass> orderBy(boolean ascending, String... columns) {
        orderBy = OrderBy.columns(columns).setAscending(ascending)
                .getQuery();
        return this;
    }

    /**
     * @param orderby The orderBy string that we use.
     * @return This WHERE query.
     */
    public Where<ModelClass> orderBy(String orderby) {
        orderBy = OrderBy.fromString(orderby).getQuery();
        return this;
    }

    /**
     * @param orderby The {@link OrderBy}
     * @return This WHERE query.
     */
    public Where<ModelClass> orderBy(OrderBy orderby) {
        orderBy = orderby.getQuery();
        return this;
    }

    /**
     * Specify the limit value you wish to use..
     *
     * @param limit The limit. E.g. 1
     * @return This WHERE query.
     */
    public Where<ModelClass> limit(Object limit) {
        this.limit = String.valueOf(limit);
        return this;
    }

    /**
     * Add an OFFSET value to this query.
     *
     * @param offset The offset value.
     * @return This WHERE query.
     */
    public Where<ModelClass> offset(Object offset) {
        this.offset = String.valueOf(offset);
        return this;
    }

    /**
     * Sets this statement to only specify that it EXISTS
     *
     * @return
     */
    public Where<ModelClass> exists(Where where) {
        conditionQueryBuilder.addCondition(Condition.exists()
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
        if ((whereBase instanceof Set) || whereBase.getQueryBuilderBase() instanceof Delete) {
            count = SQLiteCompatibilityUtils.executeUpdateDelete(databaseDefinition.getWritableDatabase(), getQuery());
        } else {
            count = DatabaseUtils.longForQuery(databaseDefinition.getWritableDatabase(), getQuery(), null);
        }
        return count;
    }

    @Override
    public String getQuery() {
        String fromQuery = whereBase.getQuery();
        QueryBuilder queryBuilder = new QueryBuilder().append(fromQuery)
                .appendQualifier("WHERE", conditionQueryBuilder.getQuery())
                .appendQualifier("GROUP BY", groupBy)
                .appendQualifier("HAVING", having.getQuery())
                .appendQualifier(null, orderBy)
                .appendQualifier("LIMIT", limit)
                .appendQualifier("OFFSET", offset);

        // Don't wast time building the string
        // unless we're going to log it.
        if (FlowLog.isEnabled(FlowLog.Level.V)) {
            FlowLog.log(FlowLog.Level.V, queryBuilder.getQuery());
        }

        return queryBuilder.getQuery();
    }

    /**
     * @return the result of the query as a {@link Cursor}.
     */
    @Override
    public Cursor query() {
        // Query the sql here
        Cursor cursor = null;
        String query = getQuery();
        if (whereBase.getQueryBuilderBase() instanceof Select) {
            cursor = databaseDefinition.getWritableDatabase()
                    .rawQuery(query, null);
        } else {
            databaseDefinition.getWritableDatabase()
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
        if (!(whereBase.getQueryBuilderBase() instanceof Select)) {
            throw new IllegalArgumentException("Please use " + methodName + "(). The beginning is not a Select");
        }
    }

    /**
     * Queries and returns only the first {@link ModelClass} result from the DB. Will enforce a limit of 1 item
     * returned from the database.
     *
     * @return The first result of this query. Note: this query forces a limit of 1 from the database.
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
     * @return if {@link android.database.Cursor}.count &lt; 0
     */
    public boolean hasData() {
        checkSelect("query");
        return SqlUtils.hasData(whereBase.getTable(), getQuery());
    }
}
