package com.dbflow5.models

import com.dbflow5.BaseUnitTest
import com.dbflow5.config.modelAdapter
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Description:
 */
class SimpleTestModelsTest : BaseUnitTest() {

    @Test
    fun validateCreationQuery() {
        assertEquals("CREATE TABLE IF NOT EXISTS `TypeConverterModel`(" +
            "`id` INTEGER, " +
            "`opaqueData` BLOB, " +
            "`blob` BLOB, " +
            "`customType` INTEGER, " +
            "PRIMARY KEY(`id`, `customType`))", modelAdapter<TypeConverterModel>().creationQuery)
    }
}