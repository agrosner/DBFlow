package com.dbflow5.models

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.adapter.createIfNotExists
import com.dbflow5.adapter.drop
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table
import com.dbflow5.config.database
import com.dbflow5.config.writableTransaction
import com.dbflow5.tempModel
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test


@Table(temporary = true, createWithDatabase = false)
class TempModel(@PrimaryKey var id: Int = 0)

class TempModelTest : BaseUnitTest() {

    @Test
    fun createTempTable() = runBlockingTest {
        database<TestDatabase>().writableTransaction {
            tempModel.createIfNotExists(this.db)
            tempModel.save(TempModel(id = 5), this.db)
            tempModel.drop(this.db)
        }
    }
}