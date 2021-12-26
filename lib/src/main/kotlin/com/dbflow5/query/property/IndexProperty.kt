package com.dbflow5.query.property

import com.dbflow5.annotation.Table
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.query.Index
import com.dbflow5.quoteIfNeeded
import kotlin.reflect.KClass

/**
 * Description: Defines an INDEX in Sqlite. It basically speeds up data retrieval over large datasets.
 * It gets generated from [Table.indexGroups], but also can be manually constructed. These are activated
 * and deactivated manually.
 */
class IndexProperty<T : Any>(
    indexName: String,
    private val unique: Boolean,
    private val table: KClass<T>,
    vararg properties: IProperty<*>
) {

    constructor(
        indexName: String,
        unique: Boolean,
        table: Class<T>,
        vararg properties: IProperty<*>
    ) : this(indexName, unique, table.kotlin, *properties)

    @Suppress("UNCHECKED_CAST")
    private val properties: Array<IProperty<*>> = properties as Array<IProperty<*>>

    val index: Index<T>
        get() = Index(indexName, table.java).on(*properties).unique(unique)

    val indexName = indexName.quoteIfNeeded() ?: ""

    fun createIfNotExists(wrapper: DatabaseWrapper) = index.createIfNotExists(wrapper)

    fun drop(wrapper: DatabaseWrapper) = index.drop(wrapper)
}

inline fun <reified T : Any> indexProperty(
    indexName: String,
    unique: Boolean,
    vararg properties: IProperty<*>
) = IndexProperty(indexName, unique, T::class, *properties)
