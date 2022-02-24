package com.dbflow5.models

import com.dbflow5.TestDatabase_Database
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table
import com.dbflow5.autoIncrementingModelAdapter
import com.dbflow5.test.DatabaseTestRule
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Description:
 */
class AutoIncrementTest {

    @get:Rule
    val dbRule = DatabaseTestRule(TestDatabase_Database::create)

    @Test
    fun testCanInsertAutoIncrement() = runBlockingTest {
        dbRule {
            val model = AutoIncrementingModel()
            val incrementingModel = autoIncrementingModelAdapter.save(model)
            assertEquals(1L, incrementingModel.id)
        }
    }

    @Test
    fun testCanInsertExistingIdAutoIncrement() = runBlockingTest {
        dbRule {
            val model = AutoIncrementingModel(3)
            val incrementingModel = autoIncrementingModelAdapter.insert(model)
            assertEquals(3L, incrementingModel.id)
        }
    }
}

@Table
class AutoIncrementingModel(@PrimaryKey(autoincrement = true) var id: Long = 0)
