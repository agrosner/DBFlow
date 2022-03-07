package com.dbflow5.test.usecases

import com.dbflow5.test.DatabaseTestRule
import com.dbflow5.test.OrderCursorModel
import com.dbflow5.test.TestDatabase_Database
import kotlin.test.Test
import kotlin.test.assertEquals

class OrderCursorTest {

    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun canPerformQueryOps() = dbRule.runTest {
        val model = OrderCursorModel(
            age = 15,
            id = 0,
            name = "Order Cursor"
        )
        val retrieved = orderCursorModelAdapter.save(
            model
        )
        assertEquals(1, retrieved.id)
    }
}
