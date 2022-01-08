package com.dbflow5.migration

import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.query.index
import com.dbflow5.query.property.IProperty
import kotlin.reflect.KClass

/**
 * Description: Defines and enables an Index structurally through a migration.
 */
abstract class IndexMigration<T : Any>(
    /**
     * The table to index on
     */
    private var onTable: KClass<T>
) : BaseMigration() {

    private var unique: Boolean = false
    private val columns = arrayListOf<IProperty<*>>()

    abstract val name: String

    override fun migrate(database: DatabaseWrapper) {
        val index = index(name, onTable).unique(unique)
        columns.forEach { index.and(it) }
        database.execSQL(index.query)
    }

    /**
     * Adds a column to the underlying INDEX
     *
     * @param property The name of the column to add to the Index
     * @return This migration
     */
    fun addColumn(property: IProperty<*>) = apply {
        columns.add(property)
    }

    /**
     * Sets the INDEX to UNIQUE
     *
     * @return This migration.
     */
    fun unique() = apply {
        unique = true
    }

}
