package com.dbflow5.adapter2

import com.dbflow5.annotation.opts.InternalDBFlowApi
import kotlin.reflect.KClass


/**
 * Used by generated code.
 */
inline fun <reified Table : Any> viewAdapter(
    name: String,
    ops: QueryOps<Table>,
    noinline creationLoader: () -> CompilableQuery,
) =
    ViewAdapter(
        view = Table::class,
        ops = ops,
        name = name,
        creationLoader = creationLoader,
    )

/**
 * Represents a generated VIEW table.
 */
data class ViewAdapter<View : Any>
@InternalDBFlowApi constructor(
    val view: KClass<View>,
    private val ops: QueryOps<View>,
    override val name: String,
    private val creationLoader: () -> CompilableQuery,
) : QueryOps<View> by ops, DBRepresentable {
    override val creationSQL: CompilableQuery = creationLoader()
}