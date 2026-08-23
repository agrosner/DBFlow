package com.dbflow5.test.usecases

import com.dbflow5.longForQuery
import com.dbflow5.test.DatabaseTestRule
import com.dbflow5.test.TestForeignKeyDatabase_Database
import com.dbflow5.test.TestRule
import kotlin.test.Test
import kotlin.test.assertEquals

class TestForeignKeyDatabaseTest : TestRule() {

    val dbRule = DatabaseTestRule(TestForeignKeyDatabase_Database)

    @Test
    fun verifyForeignKeysPragmaGotEnabled() = dbRule.runTest {
        val enabled = longForQuery(db, "PRAGMA foreign_keys;")
        assertEquals(1L, enabled)
    }
}

