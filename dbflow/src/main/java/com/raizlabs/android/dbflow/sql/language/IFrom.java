package com.raizlabs.android.dbflow.sql.language;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.sql.language.property.IndexProperty;
import com.raizlabs.android.dbflow.sql.queriable.ModelQueriable;

/**
 * Description:
 */

public interface IFrom<TModel> extends WhereBase<TModel>, Transformable<TModel> {

    /**
     * Adds a join on a specific table for this query
     *
     * @param table    The table this corresponds to
     * @param joinType The type of join to use
     */
    @NonNull
    <TJoin> IJoin<TJoin, TModel> join(Class<TJoin> table, @NonNull Join.JoinType joinType);

    /**
     * Adds a join on a specific table for this query.
     *
     * @param modelQueriable A query we construct the {@link Join} from.
     * @param joinType       The type of join to use.
     */
    @NonNull
    <TJoin> IJoin<TJoin, TModel>
    join(ModelQueriable<TJoin> modelQueriable, @NonNull Join.JoinType joinType);

    /**
     * Adds a {@link Join.JoinType#CROSS} join on a specific table for this query.
     *
     * @param table   The table to join on.
     * @param <TJoin> The class of the join table.
     */
    @NonNull
    <TJoin> IJoin<TJoin, TModel> crossJoin(Class<TJoin> table);

    /**
     * Adds a {@link Join.JoinType#CROSS} join on a specific table for this query.
     *
     * @param modelQueriable The query to join on.
     * @param <TJoin>        The class of the join table.
     */
    @NonNull
    <TJoin> IJoin<TJoin, TModel> crossJoin(ModelQueriable<TJoin> modelQueriable);

    /**
     * Adds a {@link Join.JoinType#INNER} join on a specific table for this query.
     *
     * @param table   The table to join on.
     * @param <TJoin> The class of the join table.
     */
    @NonNull
    <TJoin> IJoin<TJoin, TModel> innerJoin(Class<TJoin> table);

    /**
     * Adds a {@link Join.JoinType#INNER} join on a specific table for this query.
     *
     * @param modelQueriable The query to join on.
     * @param <TJoin>        The class of the join table.
     */
    @NonNull
    <TJoin> IJoin<TJoin, TModel> innerJoin(ModelQueriable<TJoin> modelQueriable);

    /**
     * Set an alias to the table name of this {@link IFrom}.
     */
    @NonNull
    IFrom<TModel> as(String alias);

    /**
     * Adds a {@link Join.JoinType#LEFT_OUTER} join on a specific table for this query.
     *
     * @param table   The table to join on.
     * @param <TJoin> The class of the join table.
     */
    @NonNull
    <TJoin> IJoin<TJoin, TModel> leftOuterJoin(Class<TJoin> table);

    /**
     * Adds a {@link Join.JoinType#LEFT_OUTER} join on a specific table for this query.
     *
     * @param modelQueriable The query to join on.
     * @param <TJoin>        The class of the join table.
     */
    @NonNull
    <TJoin> IJoin<TJoin, TModel> leftOuterJoin(ModelQueriable<TJoin> modelQueriable);

    /**
     * @return an empty {@link Where} statement
     */
    @NonNull
    IWhere<TModel> where();

    /**
     * @param conditions The array of conditions that define this WHERE statement
     * @return A {@link Where} statement with the specified array of {@link Condition}.
     */
    @NonNull
    IWhere<TModel> where(SQLCondition... conditions);

    /**
     * Begins an INDEXED BY piece of this query with the specified name.
     *
     * @param indexProperty The index property generated.
     */
    @NonNull
    IndexedBy<TModel> indexedBy(IndexProperty<TModel> indexProperty);
}
