package com.dbflow5.rx2.query

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.config.writableTransaction
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.query.delete
import com.dbflow5.query.insert
import com.dbflow5.query.select
import com.dbflow5.reactivestreams.query.queryStreamResults
import com.dbflow5.reactivestreams.transaction.asFlowable
import com.dbflow5.simpleModel
import com.dbflow5.structure.delete
import com.dbflow5.structure.insert
import com.dbflow5.structure.save
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CursorResultSubscriberTest : BaseUnitTest() {

    @Test
    fun testCanQueryStreamResults() = runBlockingTest {
        database<TestDatabase>().writableTransaction {
            (0..9).forEach { simpleModel.save(SimpleModel("$it")) }

            var count = 0
            (select from SimpleModel::class)
                .queryStreamResults(this.db)
                .subscribe {
                    count++
                    assert(it != null)
                }

            assertEquals(10, count)
        }
    }

    @Test
    fun testCanObserveOnTableChangesWithModelOps() = runBlockingTest {
        var count = 0
        (select from SimpleModel::class)
            .asFlowable(database<TestDatabase>()) { db -> queryList(db) }
            .subscribe {
                count++
            }
        val model = SimpleModel("test")
        database<TestDatabase>().executeTransaction { db ->
            model.save(db)
            model.delete(db)
            model.insert(db)
        }
        assertEquals(2, count) // once for subscription, 1 for operations in transaction.
    }

    @Test
    fun testCanObserveOnTableChangesWithTableOps() = runBlockingTest {
        database<TestDatabase> { db ->
            delete<SimpleModel>().executeUpdateDelete(db)
            var count = 0
            var curList: List<SimpleModel> = arrayListOf()
            (select from SimpleModel::class)
                .asFlowable(db) { queryList(it) }
                .subscribe {
                    curList = it
                    count++
                }
            db.writableTransaction {
                insert(SimpleModel::class, SimpleModel_Table.name)
                    .values("test")
                    .executeInsert()
                insert(SimpleModel::class, SimpleModel_Table.name)
                    .values("test1")
                    .executeInsert()
                insert(SimpleModel::class, SimpleModel_Table.name)
                    .values("test2")
                    .executeInsert()
            }


            assertEquals(3, curList.size)

            val model = (select
                from SimpleModel::class
                where SimpleModel_Table.name.eq("test"))
                .requireSingle(db)

            db.executeTransaction { d -> model.delete(d) }
            db.tableObserver.checkForTableUpdates()

            assertEquals(2, curList.size)
            assertEquals(3, count) // once for subscription, 2 for transactions
        }
    }

}