package com.dbflow5.sql.language

import com.dbflow5.TestDatabase_Database
import com.dbflow5.assertEquals
import com.dbflow5.query.operations.UnSafeStringOperator
import com.dbflow5.query.select
import com.dbflow5.simpleModelAdapter
import com.dbflow5.test.DatabaseTestRule
import org.junit.Rule
import org.junit.Test

class UnSafeStringOperatorTest {

    @get:Rule
    val dbRule = DatabaseTestRule(TestDatabase_Database::create)

    @Test
    fun testCanIncludeInQuery() = dbRule.runBlockingTest {
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