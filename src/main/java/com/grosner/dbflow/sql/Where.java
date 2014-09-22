package com.grosner.dbflow.sql;

import android.database.DatabaseUtils;

import com.grosner.dbflow.config.FlowLog;
import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.sql.builder.QueryBuilder;
import com.grosner.dbflow.sql.builder.WhereQueryBuilder;
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
    private WhereQueryBuilder<ModelClass> mWhereQueryBuilder;

    /**
     * The SQL GROUP BY method
     */
    private String mGroupBy;

    /**
     * The SQL HAVING
     */
    private String mHaving;

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
     * Constructs this class with the specified {@link com.grosner.dbflow.config.FlowManager}
     * and {@link com.grosner.dbflow.sql.From} chunk
     *
     * @param manager The database manager
     * @param from    The FROM statement chunk
     */
    public Where(FlowManager manager, From<ModelClass> from) {
        mManager = manager;
        mFrom = from;
        mWhereQueryBuilder = new WhereQueryBuilder<ModelClass>(mManager, mFrom.getTable());
    }

    /**
     * Defines the full SQL clause for the WHERE statement
     *
     * @param whereClause The SQL after WHERE . ex: columnName = "name" AND ID = 0
     * @return
     */
    public Where<ModelClass> whereClause(String whereClause) {
        mWhereQueryBuilder.append(whereClause);
        return this;
    }

    /**
     * Defines the {@link com.grosner.dbflow.sql.builder.WhereQueryBuilder} that will build this SQL statement
     *
     * @param whereQueryBuilder Helps build the SQL after WHERE
     * @return
     */
    public Where<ModelClass> whereQuery(WhereQueryBuilder<ModelClass> whereQueryBuilder) {
        mWhereQueryBuilder = whereQueryBuilder;
        return this;
    }

    /**
     * Adds a param to the WHERE clause with the "=" operator
     *
     * @param columnName The name of column
     * @param value      the value of column
     * @return
     */
    public Where<ModelClass> param(String columnName, Object value) {
        mWhereQueryBuilder.param(columnName, value);
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
    public Where<ModelClass> param(String columnName, String operator, Object value) {
        mWhereQueryBuilder.param(columnName, operator, value);
        return this;
    }

    /**
     * Adds a param to the WHERE clause with the custom {@link com.grosner.dbflow.sql.builder.WhereQueryBuilder.WhereParam}
     *
     * @param whereParam The {@link com.grosner.dbflow.sql.builder.WhereQueryBuilder.WhereParam} to use
     * @return
     */
    public Where<ModelClass> param(WhereQueryBuilder.WhereParam whereParam) {
        mWhereQueryBuilder.param(whereParam);
        return this;
    }

    /**
     * Adds a bunch of {@link com.grosner.dbflow.sql.builder.WhereQueryBuilder.WhereParam} to this builder.
     *
     * @param params The map of {@link com.grosner.dbflow.sql.builder.WhereQueryBuilder.WhereParam}
     * @return
     */
    public Where<ModelClass> params(Map<String, WhereQueryBuilder.WhereParam> params) {
        mWhereQueryBuilder.params(params);
        return this;
    }

    /**
     * Adds primary key params to the where. They must be in order that the primary keys are defined.
     *
     * @param values the values of the primary keys from the table. Must be in order of the primary key declaration.
     * @return
     */
    public Where<ModelClass> primaryParams(Object... values) {
        mWhereQueryBuilder.primaryParams(values);
        return this;
    }

    /**
     * Defines a SQL GROUP BY statement without the GROUP BY.
     *
     * @param groupBy
     * @return
     */
    public Where<ModelClass> groupBy(String groupBy) {
        mGroupBy = groupBy;
        return this;
    }

    /**
     * Defines a SQL HAVING statement without the HAVING.
     *
     * @param having
     * @return
     */
    public Where<ModelClass> having(String having) {
        mHaving = having;
        return this;
    }

    /**
     * Defines a SQL ORDER BY statement without the ORDER BY.
     *
     * @param orderBy
     * @return
     */
    public Where<ModelClass> orderBy(String orderBy) {
        mOrderBy = orderBy;
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
     * Run this query without expecting any results
     */
    public void query() {
        // Query the sql here
        mManager.getWritableDatabase().rawQuery(getQuery(), null);
    }

    /**
     * Queries for all of the results this statement returns from a DB cursor in the form of the {@link ModelClass}
     *
     * @return All of the entries in the DB converted into {@link ModelClass}
     */
    public List<ModelClass> queryList() {
        if (mFrom.getQueryBuilderBase() instanceof Select) {
            return SqlUtils.queryList(mManager, mFrom.getTable(), getQuery());
        } else {
            throw new IllegalArgumentException("Please use query(). The Querybase is not a Select");
        }
    }

    /**
     * Queries and returns only the first {@link ModelClass} result from the DB.
     *
     * @return The first result of this query. Note: this query may return more than one from the DB.
     */
    public ModelClass querySingle() {
        if (mFrom.getQueryBuilderBase() instanceof Select) {
            return SqlUtils.querySingle(mManager, mFrom.getTable(), getQuery());
        } else {
            throw new IllegalArgumentException("Please use query(). The Querybase is not a Select");
        }
    }

    /**
     * Returns whether the DB {@link android.database.Cursor} returns with a count of at least 1
     *
     * @return if {@link android.database.Cursor}.count > 0
     */
    public boolean hasData() {
        if (mFrom.getQueryBuilderBase() instanceof Select) {
            return SqlUtils.hasData(mManager, mFrom.getTable(), getQuery());
        } else {
            throw new IllegalArgumentException("Please use query(). The Querybase is not a Select");
        }
    }

    @Override
    public String getQuery() {
        String fromQuery = mFrom.getQuery();
        QueryBuilder queryBuilder = new QueryBuilder().append(fromQuery).appendSpace();

        queryBuilder.appendQualifier("WHERE", mWhereQueryBuilder.getQuery())
                .appendQualifier("GROUP BY", mGroupBy)
                .appendQualifier("HAVING", mHaving)
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
