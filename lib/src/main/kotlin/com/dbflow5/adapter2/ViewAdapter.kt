package com.dbflow5.adapter2

import com.dbflow5.annotation.opts.InternalDBFlowApi
import kotlin.reflect.KClass


/**
 * Used by generated code.
 */
inline fun <reified Table : Any> viewAdapter(ops: QueryOps<Table>) =
    ViewAdapter(
        view = Table::class,
        ops = ops,
    )

/**
 * Represents a generated VIEW table.
 */
data class ViewAdapter<View : Any>
@InternalDBFlowApi constructor(
    val view: KClass<View>,
    private val ops: QueryOps<View>
) : QueryOps<View> by ops