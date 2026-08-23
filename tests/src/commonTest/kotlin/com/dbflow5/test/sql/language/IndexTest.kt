package com.dbflow5.test.sql.language

import com.dbflow5.test.TestDatabase_Database
import com.dbflow5.test.SimpleModel
import com.dbflow5.test.TwoColumnModel
import com.dbflow5.query.createIndexOn
import com.dbflow5.test.DatabaseTestRule
import com.dbflow5.test.TestRule
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class IndexTest : TestRule()  {

    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun validateBasicIndex() = runTest {
        dbRule {
            assertEquals(
                "CREATE INDEX IF NOT EXISTS `index` ON `SimpleModel`(`name`)",
                simpleModelAdapter.createIndexOn(
                    name = "index",
                    property = SimpleModel.name
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
                    property = SimpleModel.name,
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
                    TwoColumnModel.name,
                    TwoColumnModel.id,
                )
                    .unique()
                    .query
            )
        }
    }
}