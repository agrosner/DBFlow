package com.dbflow5.test.usecases

import com.dbflow5.adapter.create
import com.dbflow5.adapter.drop
import com.dbflow5.test.DatabaseTestRule
import com.dbflow5.test.TempModel
import com.dbflow5.test.TestDatabase_Database
import com.dbflow5.test.tempModelAdapter
import kotlin.test.Test
import kotlin.test.assertEquals

class TempModelTest {

    val dbRule = DatabaseTestRule(TestDatabase_Database)

    /**
     * Delays execution until test runs and simply creates the temp table and drops it.
     */
    @Test
    fun createTempTable() = dbRule.runTest {
        tempModelAdapter.create(db)
        val model = tempModelAdapter.save(TempModel(id = 5))
        assertEquals(TempModel(id = 5), model)
        tempModelAdapter.drop(db)
    }
}
