package com.dbflow5.adapter2

import com.dbflow5.annotation.opts.InternalDBFlowApi
import kotlin.reflect.KClass


/**
 * Used by generated code.
 */
inline fun <reified Table : Any> viewAdapter(
    name: String,
    ops: QueryOps<Table>,
    createWithDatabase: Boolean,
    noinline creationLoader: () -> CompilableQuery,
) =
    ViewAdapter(
        view = Table::class,
        ops = ops,
        name = name,
        creationLoader = creationLoader,
        createWithDatabase = createWithDatabase,
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
    override val createWithDatabase: Boolean,
) : QueryOps<View> by ops, DBRepresentable<View> {
    override val dropSQL: CompilableQuery = CompilableQuery(
        "DROP VIEW $name IF EXISTS"
    )
    override val type: KClass<View> = view
    override val creationSQL: CompilableQuery by lazy(creationLoader)
}