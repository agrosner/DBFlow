package com.dbflow5.prepackaged

import com.dbflow5.annotation.Column
import com.dbflow5.annotation.Database
import com.dbflow5.annotation.Migration
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table
import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.migration.AlterTableMigration
import com.dbflow5.migration.BaseMigration
import com.dbflow5.query.insertInto
import com.dbflow5.query.property.propertyString
import com.dbflow5.sql.SQLiteType

@Database(
    version = 1,
    tables = [
        Dog::class,
    ]
)
abstract class PrepackagedDB : DBFlowDatabase()

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
abstract class MigratedPrepackagedDB : DBFlowDatabase() {

    @Migration(version = 2, priority = 1)
    class AddNewFieldMigration : AlterTableMigration<Dog2>(Dog2::class) {
        override fun onPreMigrate() {
            addColumn(SQLiteType.TEXT, "newField")
        }
    }

    @Migration(version = 2, priority = 2)
    class AddSomeDataMigration : BaseMigration() {
        override fun migrate(database: DatabaseWrapper) {
            insertInto<Dog2>().columnValues(
                propertyString<Dog2>("`breed`") to "NewBreed",
                propertyString<Dog2>("`newField`") to "New Field Data",
            ).executeInsert(database)
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
