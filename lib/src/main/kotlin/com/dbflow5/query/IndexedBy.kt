package com.dbflow5.query

import com.dbflow5.query.operations.IndexProperty
import com.dbflow5.sql.Query

interface IndexedBy<Table : Any> : Query

internal data class IndexedByImpl<Table : Any>(
    val indexable: Indexable<Table>,
    val property: IndexProperty<Table>,
) : IndexedBy<Table> {
    override val query: String by lazy {
        "${indexable.query.trim()} INDEXED BY ${property.name} "
    }
}