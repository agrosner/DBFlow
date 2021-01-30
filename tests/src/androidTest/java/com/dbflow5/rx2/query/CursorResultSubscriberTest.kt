package com.dbflow5.rx2.query

import com.dbflow5.BaseUnitTest
import com.dbflow5.config.databaseForTable
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.query.delete
import com.dbflow5.query.insert
import com.dbflow5.query.select
import com.dbflow5.reactivestreams.query.queryStreamResults
import com.dbflow5.reactivestreams.transaction.asFlowable
import com.dbflow5.structure.delete
import com.dbflow5.structure.insert
import com.dbflow5.structure.save
import org.junit.Assert.assertEquals
import org.junit.Test

class CursorResultSubscriberTest : BaseUnitTest() {

    @Test
    fun testCanQueryStreamResults() {
        databaseForTable<SimpleModel> { db ->
            (0..9).forEach { SimpleModel("$it").save(db) }

            var count = 0
            (select from SimpleModel::class)
                .queryStreamResults(db)
                .subscribe {
                    count++
                    assert(it != null)
                }

            assertEquals(10, count)
        }
    }

    @Test
    fun testCanObserveOnTableChangesWithModelOps() {
        var count = 0
        (select from SimpleModel::class)
            .asFlowable { db, modelQueriable -> modelQueriable.queryList(db) }
            .subscribe {
                count++
            }
        val model = SimpleModel("test")
        databaseForTable<SimpleModel>().executeTransaction { db ->
            model.save(db)
            model.delete(db)
            model.insert(db)
            assertEquals(2, count) // once for subscription, 1 for operations in transaction.
        }
    }

    @Test
    fun testCanObserveOnTableChangesWithTableOps() {
        databaseForTable<SimpleModel> { db ->
            delete<SimpleModel>().executeUpdateDelete(db)
            var count = 0
            var curList: MutableList<SimpleModel> = arrayListOf()
            (select from SimpleModel::class)
                .asFlowable { databaseWrapper, modelQueriable -> modelQueriable.queryList(databaseWrapper) }
                .subscribe {
                    curList = it
                    count++
                }
            db.executeTransaction { d ->
                insert(SimpleModel::class, SimpleModel_Table.name)
                    .values("test")
                    .executeInsert(d)
                insert(SimpleModel::class, SimpleModel_Table.name)
                    .values("test1")
                    .executeInsert(d)
                insert(SimpleModel::class, SimpleModel_Table.name)
                    .values("test2")
                    .executeInsert(d)
            }


            assertEquals(3, curList.size)

            db.executeTransaction { d ->
                val model = (select
                    from SimpleModel::class
                    where SimpleModel_Table.name.eq("test")).requireSingle(d)
                model.delete(d)
            }
            db.tableObserver.checkForTableUpdates()

            assertEquals(2, curList.size)
            assertEquals(3, count) // once for subscription, 2 for transactions
        }
    }

}