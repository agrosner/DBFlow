package com.dbflow5.test.sql.language

import com.dbflow5.test.TestDatabase_Database
import com.dbflow5.assertEquals
import com.dbflow5.query.operations.UnSafeStringOperator
import com.dbflow5.query.select
import com.dbflow5.test.simpleModelAdapter
import com.dbflow5.test.DatabaseTestRule
import org.junit.Rule
import kotlin.test.Test

class UnSafeStringOperatorTest {

    
    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun testCanIncludeInQuery() = dbRule.runTest {
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