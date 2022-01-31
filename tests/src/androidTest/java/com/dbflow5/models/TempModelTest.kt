package com.dbflow5.models

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.adapter.createIfNotExists
import com.dbflow5.adapter.drop
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table
import com.dbflow5.config.database
import com.dbflow5.tempModel
import org.junit.Test


@Table(temporary = true, createWithDatabase = false)
class TempModel(@PrimaryKey var id: Int = 0)

class TempModelTest : BaseUnitTest() {

    @Test
    fun createTempTable() {
        database<TestDatabase> {
            tempModel.createIfNotExists(db)
            tempModel.save(TempModel(id = 5), db)
            tempModel.drop(db)
        }
    }
}