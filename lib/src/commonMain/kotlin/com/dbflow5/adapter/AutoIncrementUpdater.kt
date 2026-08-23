package com.dbflow5.adapter

/**
 * Updates auto increment field by mutating or copying the autoincrementing column with specified id.
 */
fun interface AutoIncrementUpdater<Table : Any> {

    fun Table.update(id: Number): Table
}

fun <Table : Any> emptyAutoIncrementUpdater() = AutoIncrementUpdater<Table> { this }