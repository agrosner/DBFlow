package com.raizlabs.dbflow5.query.property

import kotlin.reflect.KClass
import com.raizlabs.dbflow5.annotation.Table
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.query.Index
import com.raizlabs.dbflow5.quoteIfNeeded

/**
 * Description: Defines an INDEX in Sqlite. It basically speeds up data retrieval over large datasets.
 * It gets generated from [Table.indexGroups], but also can be manually constructed. These are activated
 * and deactivated manually.
 */
class IndexProperty<T : Any>(indexName: String,
                             private val unique: Boolean,
                             private val table: KClass<T>,
                             vararg properties: IProperty<*>) {

    @Suppress("UNCHECKED_CAST")
    private val properties: Array<IProperty<*>> = properties as Array<IProperty<*>>

    val index: Index<T>
        get() = Index(indexName, table).on(*properties).unique(unique)

    val indexName = indexName.quoteIfNeeded() ?: ""

    fun createIfNotExists(wrapper: DatabaseWrapper) {
        index.enable(wrapper)
    }

    fun drop(wrapper: DatabaseWrapper) {
        index.disable(wrapper)
    }
}
