package com.dbflow5.rx2.query

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.beginTransactionAsync
import com.dbflow5.config.database
import com.dbflow5.config.writableTransaction
import com.dbflow5.database.FlowCursor
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.query2.insert
import com.dbflow5.query2.operations.Literal
import com.dbflow5.query2.select
import com.dbflow5.query2.selectCountOf
import com.dbflow5.reactivestreams.transaction.asMaybe
import com.dbflow5.reactivestreams.transaction.asSingle
import com.dbflow5.simpleModelAdapter
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RXQueryTests : BaseUnitTest() {

    @Test
    fun testCanQuery() = runBlockingTest {
        database<TestDatabase>().writableTransaction {
            simpleModelAdapter.save(SimpleModel("Name"))

            var cursor: FlowCursor? = null

            this.db.beginTransactionAsync { Result.success(simpleModelAdapter.select().cursor()) }
                .asMaybe()
                .subscribe {
                    cursor = it.getOrNull()
                }

            assertEquals(1, cursor!!.count)
            cursor!!.close()
        }
    }

    @Test
    fun testCountMethod() = runBlockingTest {
        database<TestDatabase>().writableTransaction {
            simpleModelAdapter.saveAll(
                listOf(
                    SimpleModel("name"),
                    SimpleModel("name2")
                )
            )
            var count = 0L
            this.db.beginTransactionAsync {
                simpleModelAdapter.selectCountOf(Literal.All).execute()
            }
                .asSingle()
                .subscribe { value ->
                    count = value.value
                }

            assertEquals(2, count)
        }
    }

    @Test
    fun testInsertMethod() {
        var count = 0L
        database<TestDatabase>()
            .beginTransactionAsync {
                (simpleModelAdapter.insert(
                    SimpleModel_Table.name.eq("name")
                )).execute()
            }.asSingle()
            .subscribe { c ->
                count = c
            }

        assertEquals(1, count)
    }

}