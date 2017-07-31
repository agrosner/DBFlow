package com.raizlabs.android.dbflow.models

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.kotlinextensions.insert
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