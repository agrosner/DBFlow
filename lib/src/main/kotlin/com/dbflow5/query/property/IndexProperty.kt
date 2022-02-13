package com.dbflow5.query.property

import com.dbflow5.adapter.SQLObjectAdapter
import com.dbflow5.adapter.makeLazySQLObjectAdapter
import com.dbflow5.annotation.IndexGroup
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.dropIndex
import com.dbflow5.query2.Index
import com.dbflow5.query2.createIndexOn
import com.dbflow5.quoteIfNeeded
import kotlinx.coroutines.runBlocking

/**
 * Description: Defines an INDEX in Sqlite. It basically speeds up data retrieval over large datasets.
 * It gets generated from [IndexGroup], but also can be manually constructed. These are activated
 * and deactivated manually.
 */
class IndexProperty<Table : Any>(
    indexName: String,
    private val unique: Boolean,
    private val adapter: SQLObjectAdapter<Table>,
    vararg properties: IProperty<*>
) {

    val index: Index<Table> by lazy {
        adapter.createIndexOn(
            indexName,
            properties.first(),
            *properties.slice(1..properties.lastIndex).toTypedArray()
        ).run { if (unique) unique() else this }
    }

    val indexName = indexName.quoteIfNeeded() ?: ""

    fun createIfNotExists(wrapper: DatabaseWrapper) = runBlocking { index.execute(wrapper) }

    fun drop(wrapper: DatabaseWrapper) = dropIndex(wrapper, indexName)
}

inline fun <reified T : Any> indexProperty(
    indexName: String,
    unique: Boolean,
    vararg properties: IProperty<*>
) = IndexProperty(
    indexName,
    unique,
    makeLazySQLObjectAdapter(T::class),
    *properties
)
