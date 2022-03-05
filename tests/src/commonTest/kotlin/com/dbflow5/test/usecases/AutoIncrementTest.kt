package com.dbflow5.test.usecases

import com.dbflow5.test.AutoIncrementingModel
import com.dbflow5.test.DatabaseTestRule
import com.dbflow5.test.TestDatabase_Database
import com.dbflow5.test.autoIncrementingModelAdapter
import kotlin.test.Test
import kotlin.test.assertEquals

class AutoIncrementTest {

    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun testCanInsertAutoIncrement() = dbRule.runTest {
        val model = AutoIncrementingModel(id = 0)
        val incrementingModel = autoIncrementingModelAdapter.save(model)
        assertEquals(1L, incrementingModel.id)
    }

    @Test
    fun testCanInsertExistingIdAutoIncrement() = dbRule.runTest {
        val model = AutoIncrementingModel(3)
        val incrementingModel = autoIncrementingModelAdapter.insert(model)
        assertEquals(3L, incrementingModel.id)
    }
}
