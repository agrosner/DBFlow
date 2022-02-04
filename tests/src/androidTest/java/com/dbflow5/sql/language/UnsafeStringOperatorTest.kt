package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.assertEquals
import com.dbflow5.config.database
import com.dbflow5.query.UnSafeStringOperator
import com.dbflow5.query.select
import org.junit.Test

class UnsafeStringOperatorTest : BaseUnitTest() {

    @Test
    fun testCanIncludeInQuery() {
        database<TestDatabase> { db ->
            val op = UnSafeStringOperator(
                "name = ?, id = ?, test = ?",
                arrayOf("'name'", "0", "'test'")
            )
            "name = 'name', id = 0, test = 'test'"
                .assertEquals(op)
            "SELECT * FROM `SimpleModel` WHERE name = 'name', id = 0, test = 'test'"
                .assertEquals(select from db.simpleModelAdapter where op)
        }
    }
}