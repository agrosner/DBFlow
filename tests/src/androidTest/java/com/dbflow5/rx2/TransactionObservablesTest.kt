package com.dbflow5.rx2

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.beginTransactionAsync
import com.dbflow5.config.database
import com.dbflow5.models.SimpleModel
import com.dbflow5.query.select
import com.dbflow5.reactivestreams.transaction.asSingle
import com.dbflow5.simpleModelAdapter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Description:
 */
class TransactionObservablesTest : BaseUnitTest() {

    @Test
    fun testObservableRun() {
        var successCalled = false
        var list: List<SimpleModel>? = null
        database<TestDatabase>()
            .beginTransactionAsync {
                (0 until 10).forEach {
                    simpleModelAdapter.save(SimpleModel("$it"))
                }
            }
            .asSingle()
            .doAfterSuccess {
                database<TestDatabase>()
                    .beginTransactionAsync { simpleModelAdapter.select().list() }
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