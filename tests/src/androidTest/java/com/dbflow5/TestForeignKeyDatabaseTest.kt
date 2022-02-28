package com.dbflow5

import com.dbflow5.test.DatabaseTestRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class TestForeignKeyDatabaseTest {

    @get:Rule
    val dbRule = DatabaseTestRule(TestForeignKeyDatabase_Database)

    @Test
    fun verifyDB() = runTest {
        dbRule {
            val enabled = longForQuery(db, "PRAGMA foreign_keys;")
            assert(enabled == 1L)
        }
    }
}