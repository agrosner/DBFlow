package com.dbflow5.test.usecases

import com.dbflow5.test.DatabaseTestRule
import com.dbflow5.test.TestDatabase_Database
import kotlin.test.Test
import kotlin.test.assertEquals

class TypeConverterModelTest {

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
