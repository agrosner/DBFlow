package com.dbflow5.test.usecases

import com.dbflow5.longForQuery
import com.dbflow5.test.DatabaseTestRule
import com.dbflow5.test.TestForeignKeyDatabase_Database
import kotlin.test.Test

class TestForeignKeyDatabaseTest {

    val dbRule = DatabaseTestRule(TestForeignKeyDatabase_Database)

    @Test
    fun verifyDB() = dbRule.runTest {
        val enabled = longForQuery(db, "PRAGMA foreign_keys;")
        assert(enabled == 1L)
    }
}

