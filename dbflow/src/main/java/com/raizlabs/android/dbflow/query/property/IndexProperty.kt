package com.raizlabs.android.dbflow.query.property

import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.quoteIfNeeded
import com.raizlabs.android.dbflow.query.Index
import com.raizlabs.android.dbflow.database.DatabaseWrapper

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
