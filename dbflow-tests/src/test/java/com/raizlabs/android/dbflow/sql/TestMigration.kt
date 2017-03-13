package com.raizlabs.android.dbflow.sql

import com.raizlabs.android.dbflow.sql.migration.AlterTableMigration
import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.structure.TestModel1

@com.raizlabs.android.dbflow.annotation.Migration(version = 1, database = TestDatabase::class, priority = 1)
class TestMigration(table: Class<TestModel1>) : AlterTableMigration<TestModel1>(table)
