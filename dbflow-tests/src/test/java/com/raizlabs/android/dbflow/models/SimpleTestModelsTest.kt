package com.raizlabs.android.dbflow.models

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.kotlinextensions.modelAdapter
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