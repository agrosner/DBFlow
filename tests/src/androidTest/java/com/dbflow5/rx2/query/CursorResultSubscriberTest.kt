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
import com.dbflow5.query.insert
import com.dbflow5.query.select
import com.dbflow5.reactivestreams.query.queryStreamResults
import com.dbflow5.reactivestreams.transaction.asFlowable
import com.dbflow5.simpleModelAdapter
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CursorResultSubscriberTest : BaseUnitTest() {

    @Test
    fun testCanQueryStreamResults() = runBlockingTest {
        database<TestDatabase>().writableTransaction {
            (0..9).forEach { simpleModelAdapter.save(SimpleModel("$it")) }

            var count = 0
            (select from simpleModelAdapter)
                .queryStreamResults(this.db)
                .subscribe {
                    count++
                }

            assertEquals(10, count)
        }
    }

    @Test
    fun testCanObserveOnTableChangesWithModelOps() = runBlockingTest {
        var count = 0
        val database = database<TestDatabase>()
        database.readableTransaction {
            (select from simpleModelAdapter)
                .asFlowable(database) { db -> queryList(db) }
                .subscribe {
                    count++
                }
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
            db.writableTransaction { delete(simpleModelAdapter).executeUpdateDelete() }
            var count = 0
            var curList: List<SimpleModel> = arrayListOf()
            (select from db.simpleModelAdapter)
                .asFlowable(db) { queryList(it) }
                .subscribe {
                    curList = it
                    count++
                }
            db.writableTransaction {
                insert(simpleModelAdapter, SimpleModel_Table.name)
                    .values("test")
                    .executeInsert()
                insert(simpleModelAdapter, SimpleModel_Table.name)
                    .values("test1")
                    .executeInsert()
                insert(simpleModelAdapter, SimpleModel_Table.name)
                    .values("test2")
                    .executeInsert()
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