package com.dbflow5.test.usecases

import com.dbflow5.dropIndex
import com.dbflow5.test.DatabaseTestRule
import com.dbflow5.test.IndexModel
import com.dbflow5.test.TestDatabase_Database
import com.dbflow5.test.TestRule
import com.dbflow5.test.index_firstIndex
import kotlin.test.Test
import kotlin.test.assertEquals

class IndexModelTest : TestRule()  {

    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun verifyCreationSQL() = dbRule {
        assertEquals("CREATE TABLE IF NOT EXISTS `IndexModel`(" +
            "`id` INTEGER NOT NULL ON CONFLICT FAIL, " +
            "`first_name` TEXT, " +
            "`last_name` TEXT, " +
            "`created_date` INTEGER, " +
            "`isPro` INTEGER NOT NULL ON CONFLICT FAIL, " +
            "PRIMARY KEY(`id`))", indexModelAdapter.creationSQL.query)
    }

    @Test
    fun verifyIndexSQL() = dbRule.runTest {
        IndexModel.index_firstIndex.execute()

        dropIndex(this, IndexModel.index_firstIndex.name)
    }
}