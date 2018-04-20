package com.raizlabs.dbflow5.query

import com.raizlabs.dbflow5.query.Join.JoinType
import com.raizlabs.dbflow5.sql.Query
import kotlin.reflect.KClass

actual open class From<T : Any> : InternalFrom<T> {

    actual constructor(queryBuilderBase: Query, table: KClass<T>, modelQueriable: ModelQueriable<T>?)
        : super(queryBuilderBase, table, modelQueriable)

    actual constructor(queryBuilderBase: Query, table: KClass<T>) : super(queryBuilderBase, table)

    constructor(queryBuilderBase: Query, table: Class<T>, modelQueriable: ModelQueriable<T>? = null)
        : super(queryBuilderBase, table.kotlin, modelQueriable)


    fun <TJoin : Any> join(table: Class<TJoin>, joinType: JoinType): Join<TJoin, T> = join(table.kotlin, joinType)

    fun <TJoin : Any> crossJoin(table: Class<TJoin>): Join<TJoin, T> = join(table, JoinType.CROSS)

    fun <TJoin : Any> innerJoin(table: Class<TJoin>): Join<TJoin, T> = join(table, JoinType.INNER)

    fun <TJoin : Any> leftOuterJoin(table: Class<TJoin>): Join<TJoin, T> = join(table, JoinType.LEFT_OUTER)

    fun <TJoin : Any> naturalJoin(table: Class<TJoin>): Join<TJoin, T> = join(table, JoinType.NATURAL)
}