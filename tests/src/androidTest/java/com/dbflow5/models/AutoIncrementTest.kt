package com.dbflow5.models

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table
import com.dbflow5.autoIncrementingModelAdapter
import com.dbflow5.config.database
import com.dbflow5.config.writableTransaction
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Description:
 */
class AutoIncrementTest : BaseUnitTest() {

    @Test
    fun testCanInsertAutoIncrement() = runBlockingTest {
        database<TestDatabase>().writableTransaction {
            val model = AutoIncrementingModel()
            val incrementingModel = autoIncrementingModelAdapter.save(model)
            assertEquals(1L, incrementingModel.id)
        }
    }

    @Test
    fun testCanInsertExistingIdAutoIncrement() = runBlockingTest {
        database<TestDatabase>().writableTransaction {
            val model = AutoIncrementingModel(3)
            val incrementingModel = autoIncrementingModelAdapter.insert(model)
            assertEquals(3L, incrementingModel.id)
        }
    }
}

@Table
class AutoIncrementingModel(@PrimaryKey(autoincrement = true) var id: Long = 0)
