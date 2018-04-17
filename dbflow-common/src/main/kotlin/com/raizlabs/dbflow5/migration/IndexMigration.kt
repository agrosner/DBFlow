package com.raizlabs.dbflow5.migration

import kotlin.reflect.KClass
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.query.index
import com.raizlabs.dbflow5.query.property.IProperty

/**
 * Description: Defines and enables an Index structurally through a migration.
 */
abstract class IndexMigration<TModel : Any>(
    /**
     * The table to index on
     */
    private var onTable: KClass<TModel>) : BaseMigration() {

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
