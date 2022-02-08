package com.dbflow5.rx2.query

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.config.executeTransaction
import com.dbflow5.config.readableTransaction
import com.dbflow5.config.writableTransaction
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.query.delete
import com.dbflow5.query.select
import com.dbflow5.query2.insert
import com.dbflow5.reactivestreams.transaction.asFlowable
import com.dbflow5.simpleModelAdapter
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.test.assertTrue

class CursorResultSubscriberTest : BaseUnitTest() {

    @Test
    fun testCanObserveOnTableChangesWithModelOps() = runBlockingTest {
        var count = 0
        val database = database<TestDatabase>()
        (select from database.simpleModelAdapter)
            .asFlowable(database) { db -> queryList(db) }
            .subscribe {
                count++
            }
        val model = SimpleModel("test")
        database.executeTransaction {
            simpleModelAdapter.save(model)
            simpleModelAdapter.delete(model)
            simpleModelAdapter.insert(model)
        }
        assertEquals(2, count) // once for subscription, 1 for operations in transaction.
    }

    @Test
    fun testCanObserveOnTableChangesWithTableOps() = runBlockingTest {
        database<TestDatabase> { db ->
            db.writableTransaction { simpleModelAdapter.delete().executeUpdateDelete() }
            var count = 0
            var curList: List<SimpleModel> = arrayListOf()
            (select from db.simpleModelAdapter)
                .asFlowable(db) { queryList(it) }
                .subscribe {
                    curList = it
                    count++
                }
            db.writableTransaction {
                assertTrue(
                    simpleModelAdapter.insert(SimpleModel_Table.name.eq("test"))
                        .execute() > 0
                )
                assertTrue(
                    simpleModelAdapter.insert(SimpleModel_Table.name.eq("test1"))
                        .execute() > 0
                )
                assertTrue(
                    simpleModelAdapter.insert(SimpleModel_Table.name.eq("test2"))
                        .execute() > 0
                )
            }


            assertEquals(3, curList.size)

            val model = db.readableTransaction {
                (select
                    from simpleModelAdapter
                    where SimpleModel_Table.name.eq("test"))
                    .requireSingle()
            }

            db.executeTransaction { simpleModelAdapter.delete(model) }
            db.tableObserver.checkForTableUpdates()

            assertEquals(2, curList.size)
            assertEquals(3, count) // once for subscription, 2 for transactions
        }
    }
}