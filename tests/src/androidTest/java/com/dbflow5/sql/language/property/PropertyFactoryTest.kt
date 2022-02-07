package com.dbflow5.sql.language.property

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.query.property.property
import com.dbflow5.query.property.propertyString
import com.dbflow5.query2.select
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

        // TODO: ensure enclosed query in new query lang.
        assertEquals(
            "(SELECT * FROM `SimpleModel`)",
            (database<TestDatabase>().simpleModelAdapter.select()).property.query
        )
        assertEquals("SomethingCool", propertyString<String>("SomethingCool").query)
    }
}