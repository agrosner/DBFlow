package com.dbflow5.test.usecases

import com.dbflow5.dropIndex
import com.dbflow5.test.DatabaseTestRule
import com.dbflow5.test.IndexModel_Table
import com.dbflow5.test.TestDatabase_Database
import kotlin.test.Test
import kotlin.test.assertEquals

class IndexModelTest {

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
        IndexModel_Table.index_firstIndex.execute()

        dropIndex(this, IndexModel_Table.index_firstIndex.name)
    }
}