package com.dbflow5.adapter2

import com.dbflow5.annotation.opts.InternalDBFlowApi
import com.dbflow5.query.operations.Property
import kotlin.reflect.KClass

typealias PropertyGetter<Table> = (columnName: String) -> Property<*, Table>

/**
 * Used by generated code.
 */
inline fun <reified Table : Any> modelAdapter(
    name: String,
    creationSQL: CompilableQuery,
    ops: TableOps<Table>,
    noinline propertyGetter: PropertyGetter<Table>,
) =
    ModelAdapter(
        table = Table::class,
        ops = ops,
        propertyGetter = propertyGetter,
        name = name,
        creationSQL = creationSQL,
    )

/**
 * Main table usage object. Retrieve instance of class via generated db scope methods.
 */
data class ModelAdapter<Table : Any>
@InternalDBFlowApi
constructor(
    val table: KClass<Table>,
    private val ops: TableOps<Table>,
    private val propertyGetter: PropertyGetter<Table>,
    override val name: String,
    override val creationSQL: CompilableQuery,
) : TableOps<Table> by ops, DBRepresentable {

    fun getProperty(columnName: String) = propertyGetter(columnName)
}