package com.dbflow5

import com.dbflow5.test.DatabaseTestRule
import org.junit.Rule
import kotlin.test.Test

class TestForeignKeyDatabaseTest {

    val dbRule = DatabaseTestRule(TestForeignKeyDatabase_Database)

    @Test
    fun verifyDB() = dbRule.runTest {
        val enabled = longForQuery(db, "PRAGMA foreign_keys;")
        assert(enabled == 1L)
    }
}
