package com.raizlabs.android.dbflow.sql.language;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.language.Join.JoinType;
import com.raizlabs.android.dbflow.sql.language.property.IndexProperty;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Description: The SQL FROM query wrapper that must have a {@link Query} base.
 */
public class From<TModel> extends BaseTransformable<TModel> {

    /**
     * The base such as {@link Delete}, {@link Select} and more!
     */
    @NonNull
    private Query queryBase;

    /**
     * An alias for the table
     */
    @Nullable
    private NameAlias tableAlias;

    /**
     * Enables the SQL JOIN statement
     */
    @NonNull
    private final List<Join> joins = new ArrayList<>();

    private NameAlias getTableAlias() {
        if (tableAlias == null) {
            tableAlias = new NameAlias.Builder(FlowManager.getTableName(getTable())).build();
        }
        return tableAlias;
    }

    /**
     * The SQL from statement constructed.
     *
     * @param querybase The base query we append this query to
     * @param table     The table this corresponds to
     */
    public From(@NonNull Query querybase, @NonNull Class<TModel> table) {
        super(table);
        queryBase = querybase;
    }

    /**
     * Set an alias to the table name of this {@link From}.
     */
    @NonNull
    public From<TModel> as(String alias) {
        tableAlias = getTableAlias()
            .newBuilder()
            .as(alias)
            .build();
        return this;
    }

    /**
     * Adds a join on a specific table for this query
     *
     * @param table    The table this corresponds to
     * @param joinType The type of join to use
     */
    @NonNull
    public <TJoin> Join<TJoin, TModel> join(Class<TJoin> table, @NonNull JoinType joinType) {
        Join<TJoin, TModel> join = new Join<>(this, table, joinType);
        joins.add(join);
        return join;
    }

    /**
     * Adds a join on a specific table for this query.
     *
     * @param modelQueriable A query we construct the {@link Join} from.
     * @param joinType       The type of join to use.
     */
    @NonNull
    public <TJoin> Join<TJoin, TModel>
    join(ModelQueriable<TJoin> modelQueriable, @NonNull JoinType joinType) {
        Join<TJoin, TModel> join = new Join<>(this, joinType, modelQueriable);
        joins.add(join);
        return join;
    }

    /**
     * Adds a {@link JoinType#CROSS} join on a specific table for this query.
     *
     * @param table   The table to join on.
     * @param <TJoin> The class of the join table.
     */
    @NonNull
    public <TJoin> Join<TJoin, TModel> crossJoin(Class<TJoin> table) {
        return join(table, JoinType.CROSS);
    }

    /**
     * Adds a {@link JoinType#CROSS} join on a specific table for this query.
     *
     * @param modelQueriable The query to join on.
     * @param <TJoin>        The class of the join table.
     */
    @NonNull
    public <TJoin> Join<TJoin, TModel> crossJoin(ModelQueriable<TJoin> modelQueriable) {
        return join(modelQueriable, JoinType.CROSS);
    }

    /**
     * Adds a {@link JoinType#INNER} join on a specific table for this query.
     *
     * @param table   The table to join on.
     * @param <TJoin> The class of the join table.
     */
    @NonNull
    public <TJoin> Join<TJoin, TModel> innerJoin(Class<TJoin> table) {
        return join(table, JoinType.INNER);
    }

    /**
     * Adds a {@link JoinType#INNER} join on a specific table for this query.
     *
     * @param modelQueriable The query to join on.
     * @param <TJoin>        The class of the join table.
     */
    @NonNull
    public <TJoin> Join<TJoin, TModel> innerJoin(ModelQueriable<TJoin> modelQueriable) {
        return join(modelQueriable, JoinType.INNER);
    }

    /**
     * Adds a {@link JoinType#LEFT_OUTER} join on a specific table for this query.
     *
     * @param table   The table to join on.
     * @param <TJoin> The class of the join table.
     */
    @NonNull
    public <TJoin> Join<TJoin, TModel> leftOuterJoin(Class<TJoin> table) {
        return join(table, JoinType.LEFT_OUTER);
    }

    /**
     * Adds a {@link JoinType#LEFT_OUTER} join on a specific table for this query.
     *
     * @param modelQueriable The query to join on.
     * @param <TJoin>        The class of the join table.
     */
    @NonNull
    public <TJoin> Join<TJoin, TModel> leftOuterJoin(ModelQueriable<TJoin> modelQueriable) {
        return join(modelQueriable, JoinType.LEFT_OUTER);
    }

    /**
     * Begins an INDEXED BY piece of this query with the specified name.
     *
     * @param indexProperty The index property generated.
     */
    @NonNull
    public IndexedBy<TModel> indexedBy(IndexProperty<TModel> indexProperty) {
        return new IndexedBy<>(indexProperty, this);
    }

    @Override
    public BaseModel.Action getPrimaryAction() {
        return (queryBase instanceof Delete) ? BaseModel.Action.DELETE : BaseModel.Action.CHANGE;
    }

    @Override
    public String getQuery() {
        QueryBuilder queryBuilder = new QueryBuilder()
            .append(queryBase.getQuery());
        if (!(queryBase instanceof Update)) {
            queryBuilder.append("FROM ");
        }

        queryBuilder.append(getTableAlias());

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
    @NonNull
    @Override
    public Query getQueryBuilderBase() {
        return queryBase;
    }

    /**
     * @return A list of {@link Class} that represents tables represented in this query. For every
     * {@link Join} on another table, this adds another {@link Class}.
     */
    @NonNull
    public java.util.Set<Class<?>> getAssociatedTables() {
        java.util.Set<Class<?>> tables = new LinkedHashSet<>();
        tables.add(getTable());
        for (Join join : joins) {
            tables.add(join.getTable());
        }
        return tables;
    }
}
