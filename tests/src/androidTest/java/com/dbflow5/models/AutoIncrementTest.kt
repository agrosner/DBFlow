package com.dbflow5.models

import com.dbflow5.BaseUnitTest
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table
import com.dbflow5.config.databaseForTable
import com.dbflow5.structure.insert
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Description:
 */
class AutoIncrementTest : BaseUnitTest() {

    @Test
    fun testCanInsertAutoIncrement() {
        val model = AutoIncrementingModel()
        val id = model.insert(databaseForTable<AutoIncrementingModel>())
        assertEquals(1L, id.getOrThrow().id)
    }

    @Test
    fun testCanInsertExistingIdAutoIncrement() {
        val model = AutoIncrementingModel(3)
        val id = model.insert(databaseForTable<AutoIncrementingModel>())
        assertEquals(3L, id.getOrThrow().id)
    }
}


@Table
class AutoIncrementingModel(@PrimaryKey(autoincrement = true) var id: Long = 0)