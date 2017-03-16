package com.raizlabs.android.dbflow.sql.language;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.Query;
import com.raizlabs.android.dbflow.sql.QueryBuilder;
import com.raizlabs.android.dbflow.sql.language.Join.JoinType;
import com.raizlabs.android.dbflow.sql.language.property.IProperty;
import com.raizlabs.android.dbflow.sql.language.property.IndexProperty;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Description: The SQL FROM query wrapper that must have a {@link Query} base.
 */
public class From<TModel> extends BaseModelQueriable<TModel> implements WhereBase<TModel>,
        ModelQueriable<TModel> {

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
     * Set an alias to the table name of this {@link IFrom}.
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
     * @return an empty {@link Where} statement
     */
    @NonNull
    public Where<TModel> where() {
        return new Where<>(this);
    }

    /**
     * @param conditions The array of conditions that define this WHERE statement
     * @return A {@link Where} statement with the specified array of {@link Operator}.
     */
    @NonNull
    public Where<TModel> where(SQLOperator... conditions) {
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
    @Override
    public Query getQueryBuilderBase() {
        return queryBase;
    }

    @NonNull
    public Where<TModel> groupBy(NameAlias... nameAliases) {
        return where().groupBy(nameAliases);
    }

    @NonNull
    public Where<TModel> groupBy(IProperty... properties) {
        return where().groupBy(properties);
    }

    @NonNull
    public Where<TModel> orderBy(NameAlias nameAlias, boolean ascending) {
        return where().orderBy(nameAlias, ascending);
    }

    @NonNull
    public Where<TModel> orderBy(IProperty property, boolean ascending) {
        return where().orderBy(property, ascending);
    }

    @NonNull
    public Where<TModel> orderBy(OrderBy orderBy) {
        return where().orderBy(orderBy);
    }

    @NonNull
    public Where<TModel> limit(int count) {
        return where().limit(count);
    }

    @NonNull
    public Where<TModel> offset(int offset) {
        return where().offset(offset);
    }

    @NonNull
    public Where<TModel> having(SQLOperator... conditions) {
        return where().having(conditions);
    }

    @NonNull
    public Where<TModel> orderByAll(List<OrderBy> orderBies) {
        return where().orderByAll(orderBies);
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
