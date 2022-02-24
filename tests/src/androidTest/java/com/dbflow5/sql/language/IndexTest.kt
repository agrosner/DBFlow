package com.dbflow5.sql.language

import com.dbflow5.TestDatabase_Database
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.models.TwoColumnModel_Table
import com.dbflow5.query.createIndexOn
import com.dbflow5.simpleModelAdapter
import com.dbflow5.test.DatabaseTestRule
import com.dbflow5.twoColumnModelAdapter
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class IndexTest {

    @get:Rule
    val dbRule = DatabaseTestRule(TestDatabase_Database::create)

    @Test
    fun validateBasicIndex() = runBlockingTest {
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
    fun validateWithoutExistCheck() = runBlockingTest {
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
    fun validateUniqueIndex() = runBlockingTest {
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