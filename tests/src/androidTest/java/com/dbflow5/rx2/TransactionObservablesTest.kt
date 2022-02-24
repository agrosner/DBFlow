package com.dbflow5.rx2

import com.dbflow5.TestDatabase_Database
import com.dbflow5.config.beginTransactionAsync
import com.dbflow5.models.SimpleModel
import com.dbflow5.query.select
import com.dbflow5.reactivestreams.transaction.asSingle
import com.dbflow5.simpleModelAdapter
import com.dbflow5.test.DatabaseTestRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TransactionObservablesTest {

    @get:Rule
    val dbRule = DatabaseTestRule(TestDatabase_Database::create)

    @Test
    fun testObservableRun() = dbRule {
        var successCalled = false
        var list: List<SimpleModel>? = null
        db.beginTransactionAsync {
            (0 until 10).forEach {
                simpleModelAdapter.save(SimpleModel("$it"))
            }
        }
            .asSingle()
            .doAfterSuccess {
                db.beginTransactionAsync { simpleModelAdapter.select().list() }
                    .asSingle()
                    .subscribe { loadedList: List<SimpleModel> ->
                        list = loadedList
                        successCalled = true
                    }
            }.subscribe()

        assertTrue(successCalled)
        assertEquals(10, list!!.size)
    }
}