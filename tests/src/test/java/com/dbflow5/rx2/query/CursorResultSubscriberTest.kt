package com.dbflow5.rx2.query

import com.dbflow5.BaseUnitTest
import com.dbflow5.config.databaseForTable
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.SimpleModel_Table
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
            (0..9).forEach { SimpleModel("$it").save() }

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
        model.save()

        model.delete()

        model.insert()

        assertEquals(4, count) // once for subscription, 3 for operations
    }

    @Test
    fun testCanObserveOnTableChangesWithTableOps() {
        databaseForTable<SimpleModel> { db ->
            var count = 0
            var curList: MutableList<SimpleModel> = arrayListOf()
            (select from SimpleModel::class)
                    .asFlowable { databaseWrapper, modelQueriable -> modelQueriable.queryList(databaseWrapper) }
                    .subscribe {
                        curList = it
                        count++
                    }
            insert(SimpleModel::class, SimpleModel_Table.name)
                    .values("test")
                    .executeInsert(db)
            insert(SimpleModel::class, SimpleModel_Table.name)
                    .values("test1")
                    .executeInsert(db)
            insert(SimpleModel::class, SimpleModel_Table.name)
                    .values("test2")
                    .executeInsert(db)


            assertEquals(3, curList.size)

            val model = (select
                    from SimpleModel::class
                    where SimpleModel_Table.name.eq("test")).requireSingle(db)
            model.delete()

            assertEquals(2, curList.size)

            assertEquals(5, count) // once for subscription, 4 for operations
        }
    }

}