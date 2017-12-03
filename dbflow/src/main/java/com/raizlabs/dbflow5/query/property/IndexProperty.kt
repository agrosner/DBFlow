package com.raizlabs.dbflow5.query.property

import com.raizlabs.dbflow5.annotation.Table
import com.raizlabs.dbflow5.quoteIfNeeded
import com.raizlabs.dbflow5.query.Index
import com.raizlabs.dbflow5.database.DatabaseWrapper

/**
 * Description: Defines an INDEX in Sqlite. It basically speeds up data retrieval over large datasets.
 * It gets generated from [Table.indexGroups], but also can be manually constructed. These are activated
 * and deactivated manually.
 */
class IndexProperty<T : Any>(indexName: String,
                             private val unique: Boolean,
                             private val table: Class<T>,
                             vararg properties: IProperty<*>) {

    @Suppress("UNCHECKED_CAST")
    private val properties: Array<IProperty<*>> = properties as Array<IProperty<*>>

    val DatabaseWrapper.index: Index<T>
        get() = Index<T>(this, indexName).on(table, *properties).unique(unique)

    val indexName = indexName.quoteIfNeeded() ?: ""

    fun createIfNotExists(wrapper: DatabaseWrapper) {
        wrapper.index.enable()
    }

    fun drop(wrapper: DatabaseWrapper) {
        wrapper.index.disable()
    }
}
