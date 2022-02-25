package com.dbflow5.prepackaged

import com.dbflow5.adapter2.ModelAdapter
import com.dbflow5.annotation.Column
import com.dbflow5.annotation.Database
import com.dbflow5.annotation.Migration
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table
import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.migration.AlterTableMigration
import com.dbflow5.migration.BaseMigration
import com.dbflow5.query.insert
import com.dbflow5.query.operations.literalOf
import com.dbflow5.sql.SQLiteType
import kotlinx.coroutines.runBlocking

@Database(
    version = 1,
    tables = [
        Dog::class,
    ]
)
abstract class PrepackagedDB : DBFlowDatabase() {
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
abstract class MigratedPrepackagedDB : DBFlowDatabase() {

    abstract val dog2Adapter: ModelAdapter<Dog2>

    @Migration(version = 2, priority = 1)
    class AddNewFieldMigration(dog2Adapter: ModelAdapter<Dog2>) :
        AlterTableMigration<Dog2>(dog2Adapter) {
        override fun onPreMigrate() {
            addColumn(SQLiteType.TEXT, "newField")
        }
    }

    @Migration(version = 2, priority = 2)
    class AddSomeDataMigration(
        private val dog2Adapter: ModelAdapter<Dog2>,
    ) : BaseMigration() {
        override fun migrate(database: DatabaseWrapper) {
            runBlocking {
                dog2Adapter.insert(
                    literalOf("`breed`") eq "NewBreed",
                    literalOf("`newField`") eq "New Field Data",
                ).execute(database)
            }
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
