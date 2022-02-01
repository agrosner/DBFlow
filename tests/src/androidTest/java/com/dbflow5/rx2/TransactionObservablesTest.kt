package com.dbflow5.rx2

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.models.SimpleModel
import com.dbflow5.query.select
import com.dbflow5.reactivestreams.transaction.asMaybe
import com.dbflow5.reactivestreams.transaction.asSingle
import com.dbflow5.structure.save
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
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
            .beginTransactionAsync { db: DatabaseWrapper ->
                (0 until 10).forEach {
                    SimpleModel("$it").save(db)
                }
            }
            .asSingle()
            .doAfterSuccess {
                database<TestDatabase>()
                    .beginTransactionAsync { db -> (select from SimpleModel::class).queryList(db) }
                    .asSingle()
                    .subscribe { loadedList: List<SimpleModel> ->
                        list = loadedList
                        successCalled = true
                    }
            }.subscribe()

        assertTrue(successCalled)
        assertEquals(10, list!!.size)
    }

    @Test
    fun testMaybe() {
        var simpleModel: SimpleModel? = SimpleModel()
        database<TestDatabase>()
            .beginTransactionAsync { (select from SimpleModel::class).querySingle(it) }
            .asMaybe()
            .subscribe {
                simpleModel = it
            }
        assertNull(simpleModel)
    }
}