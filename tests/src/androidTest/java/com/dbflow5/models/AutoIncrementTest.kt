package com.dbflow5.models

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table
import com.dbflow5.autoIncrementingModel
import com.dbflow5.config.database
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Description:
 */
class AutoIncrementTest : BaseUnitTest() {

    @Test
    fun testCanInsertAutoIncrement() {
        database<TestDatabase> {
            val model = AutoIncrementingModel()
            val id = autoIncrementingModel.save(model)
            assertEquals(1L, id.getOrThrow().id)
        }
    }

    @Test
    fun testCanInsertExistingIdAutoIncrement() {
        database<TestDatabase> {
            val model = AutoIncrementingModel(3)
            val id = autoIncrementingModel.insert(model)
            assertEquals(3L, id.getOrThrow().id)
        }
    }
}

@Table
class AutoIncrementingModel(@PrimaryKey(autoincrement = true) var id: Long = 0)
