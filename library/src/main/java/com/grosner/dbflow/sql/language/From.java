package com.grosner.dbflow.sql.language;

import android.database.Cursor;
import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.sql.Queriable;
import com.grosner.dbflow.sql.Query;
import com.grosner.dbflow.sql.builder.Condition;
import com.grosner.dbflow.sql.builder.ConditionQueryBuilder;
import com.grosner.dbflow.sql.QueryBuilder;
import com.grosner.dbflow.structure.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: The SQL FROM query wrapper that must have a {@link com.grosner.dbflow.sql.Query} base.
 */
public class From<ModelClass extends Model> implements WhereBase<ModelClass>, Queriable<ModelClass> {

    /**
     * The base such as {@link Delete}, {@link Select} and more!
     */
    private Query mQueryBuilderBase;

    /**
     * The table that this statement gets from
     */
    private Class<ModelClass> mTable;

    /**
     * An alias for the table
     */
    private String mAlias;

    /**
     * Enables the SQL JOIN statement
     */
    private List<Join> mJoins = new ArrayList<Join>();

    /**
     * The SQL from statement constructed.
     *
     * @param querybase The base query we append this query to
     * @param table     The table this corresponds to
     */
    public From(Query querybase, Class<ModelClass> table) {
        mQueryBuilderBase = querybase;
        mTable = table;
    }

    /**
     * The alias that this table name we use
     *
     * @param alias
     * @return
     */
    public From<ModelClass> as(String alias) {
        mAlias = alias;
        return this;
    }

    /**
     * Adds a join on a specific table for this query
     *
     * @param table    The table this corresponds to
     * @param joinType The type of join to use
     * @return
     */
    public <JoinType extends Model> Join<JoinType, ModelClass> join(Class<JoinType> table, Join.JoinType joinType) {
        Join<JoinType, ModelClass> join = new Join<JoinType, ModelClass>(this, table, joinType);
        mJoins.add(join);
        return join;
    }

    /**
     * Returns a {@link Where} statement with the sql clause
     *
     * @param whereClause The full SQL string after the WHERE keyword
     * @return
     */
    public Where<ModelClass> where(String whereClause) {
        return where().whereClause(whereClause);
    }

    /**
     * Returns an empty {@link Where} statement
     *
     * @return
     */
    public Where<ModelClass> where() {
        return new Where<ModelClass>(this);
    }

    /**
     * Returns a {@link Where} statement with the specified {@link com.grosner.dbflow.sql.builder.ConditionQueryBuilder}
     *
     * @param conditionQueryBuilder
     * @return
     */
    public Where<ModelClass> where(ConditionQueryBuilder<ModelClass> conditionQueryBuilder) {
        return where().whereQuery(conditionQueryBuilder);
    }

    /**
     * Returns a {@link Where} statement with the specified array of {@link com.grosner.dbflow.sql.builder.Condition}
     *
     * @param conditions The array of conditions that define this WHERE statement
     * @return
     */
    public Where<ModelClass> where(Condition... conditions) {
        return where().andThese(conditions);
    }

    /**
     * Run this query and returns the {@link android.database.Cursor} for it
     *
     * @return the Sqlite {@link android.database.Cursor} from this query
     */
    @Override
    public Cursor query() {
        return where().query();
    }

    /**
     * Queries for all of the results this statement returns from a DB cursor in the form of the {@link ModelClass}
     *
     * @return All of the entries in the DB converted into {@link ModelClass}
     */
    @Override
    public List<ModelClass> queryList() {
        return where().queryList();
    }

    /**
     * Queries and returns only the first {@link ModelClass} result from the DB.
     *
     * @return The first result of this query. Note: this query may return more than one from the DB.
     */
    @Override
    public ModelClass querySingle() {
        return where().querySingle();
    }

    public Set<ModelClass> set(Condition... conditions) {
        return set().conditions(conditions);
    }

    public Set<ModelClass> set() {
        if (!(mQueryBuilderBase instanceof Update)) {
            throw new IllegalStateException("Cannot use set() without an UPDATE as the base");
        }
        return new Set<ModelClass>(this, mTable);
    }

    public Set<ModelClass> set(ConditionQueryBuilder<ModelClass> conditionQueryBuilder) {
        return set().conditionQuery(conditionQueryBuilder);
    }

    @Override
    public String toString() {
        return getQuery();
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder()
                .append(mQueryBuilderBase.getQuery());
        if (!(mQueryBuilderBase instanceof Update)) {
            queryBuilder.append("FROM ");
        }

        queryBuilder.append(FlowManager.getTableName(mTable));

        if (mQueryBuilderBase instanceof Select) {
            queryBuilder.appendSpace().appendQualifier("AS", mAlias);
            for (Join join : mJoins) {
                queryBuilder.append(join.getQuery());
            }
        } else {
            queryBuilder.appendSpace();
        }

        return queryBuilder.getQuery();
    }

    /**
     * The table this From corresponds to
     *
     * @return
     */
    public Class<ModelClass> getTable() {
        return mTable;
    }

    /**
     * The base query
     *
     * @return
     */
    public Query getQueryBuilderBase() {
        return mQueryBuilderBase;
    }
}
