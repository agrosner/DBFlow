package com.raizlabs.dbflow5.dbflow.models

import com.raizlabs.dbflow5.dbflow.BaseUnitTest
import com.raizlabs.dbflow5.dbflow.TestDatabase
import com.raizlabs.dbflow5.annotation.PrimaryKey
import com.raizlabs.dbflow5.annotation.Table
import com.raizlabs.dbflow5.structure.insert
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Description:
 */
class AutoIncrementTest : BaseUnitTest() {

    @Test
    fun testCanInsertAutoIncrement() {
        val model = AutoIncrementingModel()
        model.insert()
        assertEquals(1L, model.id)
    }

    @Test
    fun testCanInsertExistingIdAutoIncrement() {
        val model = AutoIncrementingModel(3)
        model.insert()
        assertEquals(3L, model.id)
    }
}


@Table(database = TestDatabase::class)
class AutoIncrementingModel(@PrimaryKey(autoincrement = true) var id: Long = 0)