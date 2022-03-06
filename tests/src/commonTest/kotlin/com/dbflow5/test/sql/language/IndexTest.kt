package com.dbflow5.test.sql.language

import com.dbflow5.test.TestDatabase_Database
import com.dbflow5.test.SimpleModel_Table
import com.dbflow5.test.TwoColumnModel_Table
import com.dbflow5.query.createIndexOn
import com.dbflow5.test.DatabaseTestRule
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class IndexTest {

    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun validateBasicIndex() = runTest {
        dbRule {
            assertEquals(
                "CREATE INDEX IF NOT EXISTS `index` ON `SimpleModel`(`name`)",
                simpleModelAdapter.createIndexOn(
                    name = "index",
                    property = SimpleModel_Table.name
                ).query
            )
        }
    }

    @Test
    fun validateWithoutExistCheck() = runTest {
        dbRule {
            assertEquals(
                "CREATE INDEX `index` ON `SimpleModel`(`name`)",
                simpleModelAdapter.createIndexOn(
                    name = "index",
                    property = SimpleModel_Table.name,
                    ifNotExists = false,
                ).query
            )
        }
    }

    @Test
    fun validateUniqueIndex() = runTest {
        dbRule {
            assertEquals(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index` ON `TwoColumnModel`(`name`, `id`)",
                twoColumnModelAdapter.createIndexOn(
                    name = "index",
                    TwoColumnModel_Table.name,
                    TwoColumnModel_Table.id,
                )
                    .unique()
                    .query
            )
        }
    }
}