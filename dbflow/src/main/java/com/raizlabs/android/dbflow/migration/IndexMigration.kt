package com.raizlabs.android.dbflow.migration

import com.raizlabs.android.dbflow.query.index
import com.raizlabs.android.dbflow.query.property.IProperty
import com.raizlabs.android.dbflow.database.DatabaseWrapper
import java.util.*

/**
 * Description: Defines and enables an Index structurally through a migration.
 */
abstract class IndexMigration<TModel>(
        /**
         * The table to index on
         */
        private var onTable: Class<TModel>) : BaseMigration() {

    private var unique: Boolean = false
    private val columns: ArrayList<IProperty<*>> = arrayListOf()

    abstract val name: String

    override fun migrate(database: DatabaseWrapper) {
        val index = database.index<TModel>(name).on(onTable).unique(unique)
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
