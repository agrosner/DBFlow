package com.dbflow5

import com.dbflow5.config.database
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

class TestForeignKeyDatabaseTest : BaseUnitTest() {

    @Test
    fun verifyDB() = runBlockingTest {
        database<TestForeignKeyDatabase> {
            val enabled = longForQuery(db, "PRAGMA foreign_keys;")
            assert(enabled == 1L)
        }
    }
}