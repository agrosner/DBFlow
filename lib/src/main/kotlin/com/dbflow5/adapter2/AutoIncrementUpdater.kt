package com.dbflow5.adapter2

/**
 * Updates auto increment field by mutating or copying the autoincrementing column with specified id.
 */
interface AutoIncrementUpdater<Table : Any> {

    fun Table.update(id: Number): Table
}
