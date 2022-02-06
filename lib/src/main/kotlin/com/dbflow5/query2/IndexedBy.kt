package com.dbflow5.query2

import com.dbflow5.query.property.IndexProperty
import com.dbflow5.quoteIfNeeded
import com.dbflow5.sql.Query

/**
 * Description:
 */
interface IndexedBy<Table : Any> : Query {
}


internal data class IndexedByImpl<Table : Any>(
    val indexable: Indexable<Table>,
    val property: IndexProperty<Table>,
) : IndexedBy<Table> {
    override val query: String by lazy {
        "${indexable.query.trim()} INDEXED BY ${property.indexName.quoteIfNeeded()} "
    }
}