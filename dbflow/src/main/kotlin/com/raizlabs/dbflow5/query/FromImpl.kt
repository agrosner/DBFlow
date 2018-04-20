package com.raizlabs.dbflow5.query

import com.raizlabs.dbflow5.sql.Query
import kotlin.reflect.KClass

actual open class From<T : Any> : InternalFrom<T> {

    actual constructor(queryBuilderBase: Query, table: KClass<T>, modelQueriable: ModelQueriable<T>?)
        : super(queryBuilderBase, table, modelQueriable)

    actual constructor(queryBuilderBase: Query, table: KClass<T>) : super(queryBuilderBase, table)

    constructor(queryBuilderBase: Query, table: Class<T>, modelQueriable: ModelQueriable<T>? = null)
        : super(queryBuilderBase, table.kotlin, modelQueriable)
}