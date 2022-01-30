package com.dbflow5

import com.dbflow5.config.database
import org.junit.Test

class TestForeignKeyDatabaseTest : BaseUnitTest() {

    @Test
    fun verifyDB() {
        database<TestForeignKeyDatabase> {
            val enabled = longForQuery(db, "PRAGMA foreign_keys;")
            assert(enabled == 1L)
        }
    }
}