package com.raizlabs.android.dbflow.sql.language.property

import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.quoteIfNeeded
import com.raizlabs.android.dbflow.sql.language.Index
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper

/**
 * Description: Defines an INDEX in Sqlite. It basically speeds up data retrieval over large datasets.
 * It gets generated from [Table.indexGroups], but also can be manually constructed. These are activated
 * and deactivated manually.
 */
class IndexProperty<T>(indexName: String, unique: Boolean, table: Class<T>,
                       vararg properties: IProperty<*>) {

    val index: Index<T> = SQLite.index(indexName)

    val indexName: String
        get() = index.indexName.quoteIfNeeded() ?: ""

    init {
        index.on(table, *properties).unique(unique)
    }

    fun createIfNotExists(wrapper: DatabaseWrapper) {
        index.enable(wrapper)
    }

    fun createIfNotExists() {
        index.enable()
    }

    fun drop() {
        index.disable()
    }

    fun drop(writableDatabase: DatabaseWrapper) {
        index.disable(writableDatabase)
    }
}
