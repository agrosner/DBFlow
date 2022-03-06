package com.dbflow5.test.sql.language.property

import com.dbflow5.test.TestDatabase_Database
import com.dbflow5.query.operations.literalOf
import com.dbflow5.query.operations.sqlLiteralOf
import com.dbflow5.query.select

import com.dbflow5.test.DatabaseTestRule
import kotlin.test.Test
import kotlin.test.assertEquals

class LiteralTests {

    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun testPrimitives() = dbRule {
        assertEquals("'c'", sqlLiteralOf('c').query)
        assertEquals("5", sqlLiteralOf(5).query)
        assertEquals("5.0", sqlLiteralOf(5.0).query)
        assertEquals("5.0", sqlLiteralOf(5.0f).query)
        assertEquals("5", sqlLiteralOf(5L).query)
        assertEquals("5", sqlLiteralOf(5.toShort()).query)
        assertEquals("5", sqlLiteralOf(5.toByte()).query)
        val nullable: Any? = null
        assertEquals("NULL", sqlLiteralOf(nullable).query)

        assertEquals(
            "SELECT * FROM `SimpleModel`",
            sqlLiteralOf(simpleModelAdapter.select()).query
        )
        assertEquals("SomethingCool", literalOf("SomethingCool").query)
    }
}