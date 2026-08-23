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
    ViewAdapterImpl(
        view = View::class,
        ops = ops,
        viewSqlName = name,
        creationSQL = creationSQL,
        createWithDatabase = createWithDatabase,
        propertyGetter = propertyGetter,
    )

/**
 * Represents a generated view. Model-view companions implement this by extending
 * [ViewAdapterImpl].
 */
interface ViewAdapter<View : Any> : DBRepresentable<View>, QueryOps<View>

/**
 * Default [ViewAdapter] implementation used by generated adapter factories.
 */
open class ViewAdapterImpl<View : Any>
@InternalDBFlowApi
constructor(
    val view: KClass<View>,
    private val ops: QueryOps<View>,
    private val propertyGetter: PropertyGetter<View>,
    private val viewSqlName: String,
    override val creationSQL: CompilableQuery,
    override val createWithDatabase: Boolean,
) : ViewAdapter<View>, QueryOps<View> by ops, AdapterCompanion<View> {
    override val table: KClass<View> = view
    override val name: String get() = viewSqlName

    override fun sqlName(): String = viewSqlName

    override val dropSQL: CompilableQuery = CompilableQuery(
        "DROP VIEW IF EXISTS $viewSqlName"
    )
    override val type: KClass<View> = view

    override fun getProperty(columnName: String) = propertyGetter(columnName)
}
