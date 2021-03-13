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
import com.dbflow5.sql.SQLiteType
import com.dbflow5.structure.BaseModel

@Database(version = 1)
abstract class PrepackagedDB : DBFlowDatabase()

@Database(version = 2)
abstract class MigratedPrepackagedDB : DBFlowDatabase() {

    @Migration(version = 2, database = MigratedPrepackagedDB::class, priority = 1)
    class AddNewFieldMigration : AlterTableMigration<Dog2>(Dog2::class) {
        override fun onPreMigrate() {
            addColumn(SQLiteType.TEXT, "newField")
        }
    }

    @Migration(version = 2, database = MigratedPrepackagedDB::class, priority = 2)
    class AddSomeDataMigration : BaseMigration() {
        override fun migrate(database: DatabaseWrapper) {
            Dog2(breed = "NewBreed", newField = "New Field Data").insert(database)
        }
    }

}

@Table(database = PrepackagedDB::class, allFields = true)
class Dog(
    @PrimaryKey var id: Int = 0,
    @Column var breed: String? = null,
    @Column var color: String? = null,
) : BaseModel()

@Table(database = MigratedPrepackagedDB::class, allFields = true, name = "Dog")
class Dog2(
    @PrimaryKey var id: Int = 0,
    @Column var breed: String? = null,
    @Column var color: String? = null,
    @Column var newField: String? = null,
) : BaseModel()
