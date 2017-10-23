package com.raizlabs.android.dbflow.sql.migration

import com.raizlabs.android.dbflow.sql.language.Index
import com.raizlabs.android.dbflow.sql.language.property.IProperty
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper

/**
 * Description: Defines and enables an Index structurally through a migration.
 */
abstract class IndexMigration<TModel>(
        /**
         * The table to index on
         */
        private var onTable: Class<TModel>) : BaseMigration() {

    /**
     * The underlying index object.
     */
    val index: Index<TModel> by lazy { Index<TModel>(name).on(onTable) }

    abstract val name: String

    /**
     * @return the query backing this migration.
     */
    val indexQuery: String
        get() = index.query

    override fun migrate(database: DatabaseWrapper) {
        database.execSQL(index.query)
    }

    /**
     * Adds a column to the underlying INDEX
     *
     * @param property The name of the column to add to the Index
     * @return This migration
     */
    fun addColumn(property: IProperty<*>) = apply {
        index.and(property)
    }

    /**
     * Sets the INDEX to UNIQUE
     *
     * @return This migration.
     */
    fun unique() = apply {
        index.unique(true)
    }

}
