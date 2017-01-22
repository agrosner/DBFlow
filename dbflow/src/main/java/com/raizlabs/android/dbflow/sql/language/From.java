package com.raizlabs.android.dbflow.sql.language;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;
import com.raizlabs.android.dbflow.sql.language.property.IndexProperty;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Description: The SQL FROM query wrapper that must have a {@link Query} base.
 */
public class From<TModel> extends BaseModelQueriable<TModel> implements IFrom<TModel>,
        ModelQueriable<TModel> {

    /**
     * The base such as {@link Delete}, {@link Select} and more!
     */
    private Query queryBase;

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
    public From(Query querybase, Class<TModel> table) {
        super(table);
        queryBase = querybase;
        tableAlias = new NameAlias.Builder(FlowManager.getTableName(table)).build();
    }

    @Override
    public From<TModel> as(String alias) {
        tableAlias = tableAlias
                .newBuilder()
                .as(alias)
                .build();
        return this;
    }

    @Override
    public <TJoin> Join<TJoin, TModel> join(Class<TJoin> table, @NonNull Join.JoinType joinType) {
        Join<TJoin, TModel> join = new Join<>(this, table, joinType);
        joins.add(join);
        return join;
    }

    @Override
    public <TJoin> Join<TJoin, TModel>
    join(ModelQueriable<TJoin> modelQueriable, @NonNull Join.JoinType joinType) {
        Join<TJoin, TModel> join = new Join<>(this, joinType, modelQueriable);
        joins.add(join);
        return join;
    }

    @Override
    public <TJoin> Join<TJoin, TModel> crossJoin(Class<TJoin> table) {
        return join(table, Join.JoinType.CROSS);
    }

    @Override
    public <TJoin> Join<TJoin, TModel> crossJoin(ModelQueriable<TJoin> modelQueriable) {
        return join(modelQueriable, Join.JoinType.CROSS);
    }

    @Override
    public <TJoin> Join<TJoin, TModel> innerJoin(Class<TJoin> table) {
        return join(table, Join.JoinType.INNER);
    }

    @Override
    public <TJoin> Join<TJoin, TModel> innerJoin(ModelQueriable<TJoin> modelQueriable) {
        return join(modelQueriable, Join.JoinType.INNER);
    }

    @Override
    public <TJoin> Join<TJoin, TModel> leftOuterJoin(Class<TJoin> table) {
        return join(table, Join.JoinType.LEFT_OUTER);
    }

    @Override
    public <TJoin> Join<TJoin, TModel> leftOuterJoin(ModelQueriable<TJoin> modelQueriable) {
        return join(modelQueriable, Join.JoinType.LEFT_OUTER);
    }

    @Override
    public Where<TModel> where() {
        return new Where<>(this);
    }

    @Override
    public Where<TModel> where(SQLCondition... conditions) {
        return where().andAll(conditions);
    }

    @Override
    public Cursor query() {
        return where().query();
    }

    @Override
    public Cursor query(DatabaseWrapper databaseWrapper) {
        return where().query(databaseWrapper);
    }

    /**
     * Executes a SQL statement that retrieves the count of results in the DB.
     *
     * @return The number of rows this query returns
     */
    @Override
    public long count() {
        return where().count();
    }

    @Override
    public long count(DatabaseWrapper databaseWrapper) {
        return where().count(databaseWrapper);
    }

    @Override
    public long executeUpdateDelete(DatabaseWrapper databaseWrapper) {
        return where().executeUpdateDelete(databaseWrapper);
    }

    @Override
    public IndexedBy<TModel> indexedBy(IndexProperty<TModel> indexProperty) {
        return new IndexedBy<>(indexProperty, this);
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
    public Where<TModel> groupBy(NameAlias... nameAliases) {
        return where().groupBy(nameAliases);
    }

    @Override
    public Where<TModel> groupBy(IProperty... properties) {
        return where().groupBy(properties);
    }

    @Override
    public Where<TModel> orderBy(NameAlias nameAlias, boolean ascending) {
        return where().orderBy(nameAlias, ascending);
    }

    @Override
    public Where<TModel> orderBy(IProperty property, boolean ascending) {
        return where().orderBy(property, ascending);
    }

    @Override
    public Where<TModel> orderBy(OrderBy orderBy) {
        return where().orderBy(orderBy);
    }

    @Override
    public Where<TModel> limit(int count) {
        return where().limit(count);
    }

    @Override
    public Where<TModel> offset(int offset) {
        return where().offset(offset);
    }

    @Override
    public Where<TModel> having(SQLCondition... conditions) {
        return where().having(conditions);
    }

    @Override
    public Where<TModel> orderByAll(List<OrderBy> orderBies) {
        return where().orderByAll(orderBies);
    }
}
