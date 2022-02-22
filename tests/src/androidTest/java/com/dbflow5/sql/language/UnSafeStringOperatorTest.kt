package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.assertEquals
import com.dbflow5.config.database
import com.dbflow5.query.operations.UnSafeStringOperator
import com.dbflow5.query.select
import com.dbflow5.simpleModelAdapter
import org.junit.Test

class UnSafeStringOperatorTest : BaseUnitTest() {

    @Test
    fun testCanIncludeInQuery() {
        database<TestDatabase> {
            val op = UnSafeStringOperator(
                "name = ?, id = ?, test = ?",
                listOf("'name'", "0", "'test'")
            )
            "name = 'name', id = 0, test = 'test'"
                .assertEquals(op)
            "SELECT * FROM `SimpleModel` WHERE name = 'name', id = 0, test = 'test'"
                .assertEquals(simpleModelAdapter.select() where op)
        }
    }
}