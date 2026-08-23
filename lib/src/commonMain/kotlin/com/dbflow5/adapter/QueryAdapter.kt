package com.dbflow5.adapter

import com.dbflow5.annotation.opts.InternalDBFlowApi
import kotlin.reflect.KClass

/**
 * Used by generated code.
 */
inline fun <reified Table : Any> queryAdapter(ops: QueryOps<Table>) =
    QueryAdapterImpl(
        query = Table::class,
        ops = ops,
    )

/**
 * Represents a generated query model. Query companions implement this by extending
 * [QueryAdapterImpl].
 */
interface QueryAdapter<QueryType : Any> : QueryOps<QueryType>, QueryRepresentable<QueryType>

/**
 * Default [QueryAdapter] implementation used by generated adapter factories.
 */
open class QueryAdapterImpl<QueryType : Any>
@InternalDBFlowApi
constructor(
    val query: KClass<QueryType>,
    private val ops: QueryOps<QueryType>,
) : QueryAdapter<QueryType>, QueryOps<QueryType> by ops, AdapterCompanion<QueryType> {
    override val table: KClass<QueryType> = query
    override val type = query
}
