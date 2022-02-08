package com.dbflow5.migration

import com.dbflow5.adapter.SQLObjectAdapter
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.query.property.IProperty
import com.dbflow5.query2.createIndexOn
import kotlinx.coroutines.runBlocking

/**
 * Description: Defines and enables an Index structurally through a migration.
 */
abstract class IndexMigration<T : Any>(
    private val columns: List<IProperty<*>>,
    private val unique: Boolean = false,
    adapterGetter: () -> SQLObjectAdapter<T>,
) : BaseMigration() {

    protected val adapter by lazy(adapterGetter)

    abstract val name: String

    override fun migrate(database: DatabaseWrapper) {
        runBlocking {
            adapter.createIndexOn(
                name, columns.first(),
                *columns.slice(1..columns.lastIndex).toTypedArray()
            ).run { if (unique) unique() else this }
                .execute(database)
        }
    }
}
