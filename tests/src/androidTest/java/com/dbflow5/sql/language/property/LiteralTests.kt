package com.dbflow5.sql.language.property

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.query2.operations.literalOf
import com.dbflow5.query2.operations.sqlLiteralOf
import com.dbflow5.query2.select
import org.junit.Assert.assertEquals
import org.junit.Test

class LiteralTests : BaseUnitTest() {

    @Test
    fun testPrimitives() {
        assertEquals("'c'", sqlLiteralOf('c').query)
        assertEquals("5", sqlLiteralOf(5).query)
        assertEquals("5.0", sqlLiteralOf(5.0).query)
        assertEquals("5.0", sqlLiteralOf(5.0f).query)
        assertEquals("5", sqlLiteralOf(5L).query)
        assertEquals("5", sqlLiteralOf(5.toShort()).query)
        assertEquals("5", sqlLiteralOf(5.toByte()).query)
        val nullable: Any? = null
        assertEquals("NULL", sqlLiteralOf(nullable).query)

        // TODO: ensure enclosed query in new query lang.
        assertEquals(
            "(SELECT * FROM `SimpleModel`)",
            sqlLiteralOf(database<TestDatabase>().simpleModelAdapter.select()).query
        )
        assertEquals("SomethingCool", literalOf("SomethingCool").query)
    }
}