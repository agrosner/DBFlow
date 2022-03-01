package com.dbflow5.adapter

import com.dbflow5.annotation.opts.InternalDBFlowApi
import kotlin.reflect.KClass


/**
 * Used by generated code.
 */
inline fun <reified View : Any> viewAdapter(
    name: String,
    ops: QueryOps<View>,
    createWithDatabase: Boolean,
    creationSQL: CompilableQuery,
    noinline propertyGetter: PropertyGetter<View>,
) =
    ViewAdapter(
        view = View::class,
        ops = ops,
        name = name,
        creationSQL = creationSQL,
        createWithDatabase = createWithDatabase,
        propertyGetter = propertyGetter,
    )

/**
 * Represents a generated VIEW table.
 */
data class ViewAdapter<View : Any>
@InternalDBFlowApi constructor(
    val view: KClass<View>,
    private val ops: QueryOps<View>,
    private val propertyGetter: PropertyGetter<View>,
    override val name: String,
    override val creationSQL: CompilableQuery,
    override val createWithDatabase: Boolean,
) : QueryOps<View> by ops, DBRepresentable<View> {
    override val dropSQL: CompilableQuery = CompilableQuery(
        "DROP VIEW IF EXISTS $name"
    )
    override val type: KClass<View> = view

    override fun getProperty(columnName: String) = propertyGetter(columnName)
}
