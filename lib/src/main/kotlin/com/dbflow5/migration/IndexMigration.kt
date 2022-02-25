package com.dbflow5.migration

import com.dbflow5.adapter.DBRepresentable
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.query.createIndexOn
import com.dbflow5.query.operations.Property
import kotlinx.coroutines.runBlocking

/**
 * Description: Defines and enables an Index structurally through a migration.
 */
abstract class IndexMigration<Table : Any>(
    private val columns: List<Property<*, Table>>,
    private val unique: Boolean = false,
    adapterGetter: () -> DBRepresentable<Table>,
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
