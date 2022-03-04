package com.dbflow5.models

import com.dbflow5.TestDatabase_Database
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table
import com.dbflow5.autoIncrementingModelAdapter
import com.dbflow5.test.DatabaseTestRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import kotlin.test.Test

/**
 * Description:
 */
class AutoIncrementTest {

    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun testCanInsertAutoIncrement() = dbRule.runTest {
        val model = AutoIncrementingModel()
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

@Table
class AutoIncrementingModel(@PrimaryKey(autoincrement = true) var id: Long = 0)
