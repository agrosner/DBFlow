package com.dbflow5.test

import com.dbflow5.annotation.Database
import com.dbflow5.annotation.ForeignKey
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table
import com.dbflow5.database.DBFlowDatabase

@Database(
    version = 1, foreignKeyConstraintsEnforced = true,
    tables = [
        TestForeignKeyDatabase.SimpleModel2::class,
        TestForeignKeyDatabase.SimpleForeignModel::class,
    ]
)
abstract class TestForeignKeyDatabase : DBFlowDatabase<TestForeignKeyDatabase>() {

    // TODO: using SimpleModel throws duplicate error due to code gen nested classes in NameModel.
    // Name model does not preserve full simple name.
    @Table
    data class SimpleModel2(@PrimaryKey val name: String)

    @Table
    data class SimpleForeignModel(
        @PrimaryKey val id: Int,
        @ForeignKey val model: SimpleModel2?,
    )
}
