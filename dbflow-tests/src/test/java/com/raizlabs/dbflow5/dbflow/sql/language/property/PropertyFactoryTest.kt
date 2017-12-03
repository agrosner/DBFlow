package com.raizlabs.dbflow5.dbflow.sql.language.property

import com.raizlabs.dbflow5.dbflow.BaseUnitTest
import com.raizlabs.dbflow5.config.databaseForTable
import com.raizlabs.dbflow5.dbflow.models.SimpleModel
import com.raizlabs.dbflow5.query.property.property
import com.raizlabs.dbflow5.query.property.propertyString
import org.junit.Assert.assertEquals
import org.junit.Test

class PropertyFactoryTest : BaseUnitTest() {

    @Test
    fun testPrimitives() {
        assertEquals("'c'", 'c'.property.query)
        assertEquals("5", 5.property.query)
        assertEquals("5.0", 5.0.property.query)
        assertEquals("5.0", 5.0f.property.query)
        assertEquals("5", 5L.property.query)
        assertEquals("5", 5.toShort().property.query)
        assertEquals("5", 5.toByte().property.query)
        val nullable: Any? = null
        assertEquals("NULL", nullable.property.query)
        databaseForTable<SimpleModel> {
            assertEquals("(SELECT * FROM `SimpleModel`)", (select from SimpleModel::class).property.query)
        }
        assertEquals("SomethingCool", propertyString<String>("SomethingCool").query)
    }
}