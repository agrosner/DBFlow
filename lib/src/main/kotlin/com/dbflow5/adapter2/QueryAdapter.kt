package com.dbflow5.adapter2

import com.dbflow5.annotation.opts.InternalDBFlowApi
import kotlin.reflect.KClass

/**
 * Used by generated code.
 */
inline fun <reified Table : Any> queryAdapter(ops: QueryOps<Table>) =
    QueryAdapter(
        query = Table::class,
        ops = ops,
    )

/**
 * Represents a generated QueryModel object handler.
 */
data class QueryAdapter<QueryType : Any>
@InternalDBFlowApi constructor(
    val query: KClass<QueryType>,
    private val ops: QueryOps<QueryType>
) : QueryOps<QueryType> by ops, QueryRepresentable<QueryType> {
    override val type = query
}
