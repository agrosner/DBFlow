package com.dbflow5.models

import com.dbflow5.TestDatabase_Database
import com.dbflow5.adapter.create
import com.dbflow5.adapter.drop
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table
import com.dbflow5.tempModelAdapter
import com.dbflow5.test.DatabaseTestRule
import org.junit.Rule
import org.junit.Test


@Table(temporary = true, createWithDatabase = false)
class TempModel(@PrimaryKey var id: Int = 0)

class TempModelTest {

    @get:Rule
    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun createTempTable() = dbRule.runTest {
        tempModelAdapter.create(db)
        tempModelAdapter.save(TempModel(id = 5))
        tempModelAdapter.drop(db)
    }
}