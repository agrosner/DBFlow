package com.dbflow5.test

import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.adapter.migrationAdapter
import com.dbflow5.annotation.Column
import com.dbflow5.annotation.Database
import com.dbflow5.annotation.Migration
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table
import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.database.scope.MigrationScope
import com.dbflow5.query.insert
import com.dbflow5.query.migration.ColumnAlter
import com.dbflow5.query.migration.alterTable
import com.dbflow5.query.operations.literalOf
import com.dbflow5.sql.SQLiteType

@Database(
    version = 1,
    tables = [
        Dog::class,
    ]
)
abstract class PrepackagedDB : DBFlowDatabase<PrepackagedDB>() {
    abstract val dogAdapter: ModelAdapter<Dog>
}

@Database(
    version = 2,
    tables = [
        Dog2::class,
    ],
    migrations = [
        MigratedPrepackagedDB.AddNewFieldMigration::class,
        MigratedPrepackagedDB.AddSomeDataMigration::class,
    ]
)
abstract class MigratedPrepackagedDB : DBFlowDatabase<MigratedPrepackagedDB>() {

    abstract val dog2Adapter: ModelAdapter<Dog2>

    @Migration(version = 2, priority = 1)
    class AddNewFieldMigration : com.dbflow5.database.Migration {
        override suspend fun MigrationScope.migrate(database: DatabaseWrapper) {
            (alterTable("Dog") addColumn ColumnAlter.Plain(
                name = "newField",
                type = SQLiteType.TEXT,
            )).execute()
        }
    }

    @Migration(version = 2, priority = 2)
    class AddSomeDataMigration : com.dbflow5.database.Migration {
        override suspend fun MigrationScope.migrate(database: DatabaseWrapper) {
            migrationAdapter("Dog").insert(
                literalOf("`breed`") eq "NewBreed",
                literalOf("`newField`") eq "New Field Data",
            ).execute(database)
        }
    }
}

@Table
class Dog(
    @PrimaryKey var id: Int = 0,
    @Column var breed: String? = null,
    @Column var color: String? = null,
)

@Table(name = "Dog")
class Dog2(
    @PrimaryKey var id: Int = 0,
    @Column var breed: String? = null,
    @Column var color: String? = null,
    @Column var newField: String? = null,
)
