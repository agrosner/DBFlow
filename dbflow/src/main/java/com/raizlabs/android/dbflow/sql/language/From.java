package com.raizlabs.android.dbflow.sql.language;

import android.database.Cursor;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.list.FlowCursorList;
import com.raizlabs.android.dbflow.list.FlowQueryList;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;
import com.raizlabs.android.dbflow.sql.language.property.IndexProperty;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;
import com.raizlabs.android.dbflow.structure.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: The SQL FROM query wrapper that must have a {@link Query} base.
 */
public class From<ModelClass extends Model> extends BaseModelQueriable<ModelClass> implements
        WhereBase<ModelClass>, ModelQueriable<ModelClass>, Transformable<ModelClass> {

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
    private NameAlias tableAlias;

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
        tableAlias = new NameAlias(FlowManager.getTableName(table));
    }

    /**
     * The alias that this table name we use
     *
     * @param alias
     * @return This FROM statement
     */
    public From<ModelClass> as(String alias) {
        tableAlias.as(alias);
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
     * @return an empty {@link Where} statement
     */
    public Where<ModelClass> where() {
        return new Where<>(this);
    }

    /**
     * @param conditions The array of conditions that define this WHERE statement
     * @return A {@link Where} statement with the specified array of {@link Condition}.
     */
    public Where<ModelClass> where(SQLCondition... conditions) {
        return where().andAll(conditions);
    }

    /**
     * @return the result of the query as a {@link Cursor}.
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
     * @return The first result of this query. It forces a {@link Where#limit(int)} of 1 for more efficient querying.
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
     * @param indexProperty The index property generated.
     * @return An INDEXED BY piece of this statement
     */
    public IndexedBy<ModelClass> indexedBy(IndexProperty<ModelClass> indexProperty) {
        return new IndexedBy<>(indexProperty, this);
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

        queryBuilder.append(tableAlias);

        if (queryBase instanceof Select) {
            for (Join join : joins) {
                queryBuilder.appendSpace();
                queryBuilder.append(join.getQuery());
            }
        } else {
            queryBuilder.appendSpace();
        }

        return queryBuilder.getQuery();
    }

    /**
     * @return The base query, usually a {@link Delete}, {@link Select}, or {@link Update}
     */
    @Override
    public Query getQueryBuilderBase() {
        return queryBase;
    }

    @Override
    public Where<ModelClass> groupBy(NameAlias... nameAliases) {
        return where().groupBy(nameAliases);
    }

    @Override
    public Where<ModelClass> groupBy(IProperty... properties) {
        return where().groupBy(properties);
    }

    @Override
    public Where<ModelClass> orderBy(NameAlias nameAlias, boolean ascending) {
        return where().orderBy(nameAlias, ascending);
    }

    @Override
    public Where<ModelClass> orderBy(IProperty property, boolean ascending) {
        return where().orderBy(property, ascending);
    }

    @Override
    public Where<ModelClass> orderBy(OrderBy orderBy) {
        return where().orderBy(orderBy);
    }

    @Override
    public Where<ModelClass> limit(int count) {
        return where().limit(count);
    }

    @Override
    public Where<ModelClass> offset(int offset) {
        return where().offset(offset);
    }

    @Override
    public Where<ModelClass> having(SQLCondition... conditions) {
        return where().having(conditions);
    }
}
