package com.dbflow5.models

import com.dbflow5.TestDatabase_Database
import com.dbflow5.test.DatabaseTestRule
import com.dbflow5.typeConverterModelAdapter
import org.junit.Assert.assertEquals
import kotlin.test.Test

/**
 * Description:
 */
class SimpleTestModelsTest {

    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun validateCreationQuery() = dbRule {
        assertEquals(
            "CREATE TABLE IF NOT EXISTS `TypeConverterModel`(" +
                "`id` INTEGER NOT NULL ON CONFLICT FAIL, " +
                "`opaqueData` BLOB, " +
                "`blob` BLOB, " +
                "`customType` INTEGER, " +
                "PRIMARY KEY(`id`, `customType`))",
            typeConverterModelAdapter.creationSQL.query
        )
    }
}