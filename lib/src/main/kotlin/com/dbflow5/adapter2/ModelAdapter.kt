package com.dbflow5.adapter2

import com.dbflow5.annotation.opts.InternalDBFlowApi
import com.dbflow5.query.operations.Property
import kotlin.reflect.KClass

typealias PropertyGetter<Table> = (columnName: String) -> Property<*, Table>

/**
 * Used by generated code.
 */
inline fun <reified Table : Any> modelAdapter(
    ops: TableOps<Table>,
    noinline propertyGetter: PropertyGetter<Table>,
) =
    ModelAdapter(
        table = Table::class,
        ops = ops,
        propertyGetter = propertyGetter,
    )

/**
 * Main hook into [TableOps], these implementations are generated
 */
data class ModelAdapter<Table : Any>
@InternalDBFlowApi
constructor(
    val table: KClass<Table>,
    private val ops: TableOps<Table>,
    private val propertyGetter: PropertyGetter<Table>,
) : TableOps<Table> by ops {

    fun getProperty(columnName: String) = propertyGetter(columnName)
}