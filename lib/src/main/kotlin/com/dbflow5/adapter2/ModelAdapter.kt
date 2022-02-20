package com.dbflow5.adapter2

import com.dbflow5.query.operations.Property

/**
 * Main hook into [TableOps], these implementations are generated
 */
abstract class ModelAdapter<Table : Any>(
    private val ops: TableOps<Table>,
    private val propertyGetter: (columnName: String) -> Property<*, Table>,
) : TableOps<Table> by ops {

    fun getProperty(columnName: String) = propertyGetter(columnName)
}