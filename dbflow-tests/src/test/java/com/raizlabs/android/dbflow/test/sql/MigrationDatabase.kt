package com.raizlabs.android.dbflow.test.sql

import com.raizlabs.android.dbflow.annotation.Database
import com.raizlabs.android.dbflow.annotation.Migration
import com.raizlabs.android.dbflow.sql.language.property.IndexProperty
import com.raizlabs.android.dbflow.sql.migration.BaseMigration
import com.raizlabs.android.dbflow.sql.migration.IndexMigration
import com.raizlabs.android.dbflow.sql.migration.IndexPropertyMigration
import com.raizlabs.android.dbflow.sql.migration.UpdateTableMigration
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper
import com.raizlabs.android.dbflow.test.sql.index.IndexModel_Table

/**
 * Description:
 */
@Database(version = 2, name = MigrationDatabase.NAME)
object MigrationDatabase {

    val NAME = "Migrations"

    @Migration(version = 2, database = MigrationDatabase::class)
    class Migration2 : BaseMigration() {

        override fun migrate(database: DatabaseWrapper) {

        }
    }

    @Migration(version = 2, priority = 0, database = MigrationDatabase::class)
    class IndexMigration2(onTable: Class<MigrationModel>) : IndexMigration<MigrationModel>(onTable) {

        override fun getName(): String {
            return "TestIndex"
        }
    }

    @Migration(version = 2, priority = 1, database = MigrationDatabase::class)
    class IndexPropertyMigration2 : IndexPropertyMigration() {

        override fun getIndexProperty(): IndexProperty<*> {
            return IndexModel_Table.index_customIndex
        }
    }

    @Migration(version = 2, priority = 2, database = MigrationDatabase::class)
    class UpdateMigration2
    /**
     * Creates an update migration.

     * @param table The table to update
     */
    (table: Class<MigrationModel>) : UpdateTableMigration<MigrationModel>(table) {

        init {
            set(MigrationModel_Table.name.eq("New Name"))
        }

    }
}
