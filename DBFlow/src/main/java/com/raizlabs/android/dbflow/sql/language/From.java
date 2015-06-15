package com.raizlabs.android.dbflow.sql.language;

import android.database.Cursor;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.list.FlowCursorList;
import com.raizlabs.android.dbflow.list.FlowQueryList;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder;
import com.raizlabs.android.dbflow.sql.builder.SQLCondition;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.ModelAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: The SQL FROM query wrapper that must have a {@link com.raizlabs.android.dbflow.sql.Query} base.
 */
public class From<ModelClass extends Model> extends BaseModelQueriable<ModelClass> implements WhereBase<ModelClass>, ModelQueriable<ModelClass> {

    /**
     * The base such as {@link Delete}, {@link Select} and more!
     */
    private Query queryBase;

    /**
     * The table that this statement gets from
     */
    private Class<ModelClass> table;

    /**
     * An alias for the table
     */
    private String tableAlias;

    /**
     * Enables the SQL JOIN statement
     */
    private List<Join> joins = new ArrayList<>();

    /**
     * The SQL from statement constructed.
     *
     * @param querybase The base query we append this query to
     * @param table     The table this corresponds to
     */
    public From(Query querybase, Class<ModelClass> table) {
        super(table);
        queryBase = querybase;
        this.table = table;
    }

    /**
     * The alias that this table name we use
     *
     * @param alias
     * @return This FROM statement
     */
    public From<ModelClass> as(String alias) {
        tableAlias = alias;
        return this;
    }

    /**
     * Adds a join on a specific table for this query
     *
     * @param table    The table this corresponds to
     * @param joinType The type of join to use
     * @return The join contained in this FROM statement
     */
    public <JoinType extends Model> Join<JoinType, ModelClass> join(Class<JoinType> table, Join.JoinType joinType) {
        Join<JoinType, ModelClass> join = new Join<>(this, table, joinType);
        joins.add(join);
        return join;
    }

    /**
     * @param ids A list of ids (in order of declaration) by which we replace into the primary WHERE query
     *            from a {@link ModelAdapter#createPrimaryModelWhere()}. The length and order MUST match
     *            the order defined in the corresponding {@link ModelAdapter} for this class.
     * @return A {@link Where} with a WHERE based on the primary keys specified.
     */
    public Where<ModelClass> byIds(Object... ids) {
        return where().whereQuery(FlowManager.getPrimaryWhereQuery(table).replaceEmptyParams(ids));
    }

    /**
     * Returns a {@link Where} statement with the sql clause
     *
     * @param whereClause The full SQL string after the WHERE keyword
     * @param args        The arguments to append
     * @return The WHERE piece of the query
     */
    public Where<ModelClass> where(String whereClause, Object... args) {
        return where().whereClause(whereClause, args);
    }

    /**
     * @return an empty {@link Where} statement
     */
    public Where<ModelClass> where() {
        return new Where<>(this);
    }

    /**
     * Returns a {@link Where} statement with the specified {@link com.raizlabs.android.dbflow.sql.builder.ConditionQueryBuilder}
     *
     * @param conditionQueryBuilder The builder of a specific set of conditions used in this query
     * @return A where statement.
     */
    public Where<ModelClass> where(ConditionQueryBuilder<ModelClass> conditionQueryBuilder) {
        return where().whereQuery(conditionQueryBuilder);
    }

    /**
     * Returns a {@link Where} statement with the specified array of {@link com.raizlabs.android.dbflow.sql.builder.Condition}
     *
     * @param conditions The array of conditions that define this WHERE statement
     * @return A where statement.
     */
    public Where<ModelClass> where(SQLCondition... conditions) {
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

    @Override
    public void queryClose() {
        Cursor query = query();
        if (query != null) {
            query.close();
        }
    }

    @Override
    public FlowCursorList<ModelClass> queryCursorList() {
        return new FlowCursorList<>(false, this);
    }

    @Override
    public FlowQueryList<ModelClass> queryTableList() {
        return new FlowQueryList<>(this);
    }

    /**
     * Executes a SQL statement that retrieves the count of results in the DB.
     *
     * @return The number of rows this query returns
     */
    public long count() {
        return where().count();
    }

    /**
     * Begins an INDEXED BY piece of this query with the specified name.
     *
     * @param indexName The name of the index.
     * @return An INDEXED BY piece of this statement
     */
    public IndexedBy<ModelClass> indexedBy(String indexName) {
        return new IndexedBy<>(indexName, this);
    }

    @Override
    public String toString() {
        return getQuery();
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder()
                .append(queryBase.getQuery());
        if (!(queryBase instanceof Update)) {
            queryBuilder.append("FROM ");
        }

        queryBuilder.appendQuoted(FlowManager.getTableName(table));

        if (queryBase instanceof Select) {
            queryBuilder.appendSpace().appendQualifier("AS", tableAlias);
            for (Join join : joins) {
                queryBuilder.append(join.getQuery());
            }
        } else {
            queryBuilder.appendSpace();
        }

        return queryBuilder.getQuery();
    }

    /**
     * @return The base query, usually a {@link com.raizlabs.android.dbflow.sql.language.Delete}.
     * {@link com.raizlabs.android.dbflow.sql.language.Select}, or {@link com.raizlabs.android.dbflow.sql.language.Update}
     */
    public Query getQueryBuilderBase() {
        return queryBase;
    }

}
