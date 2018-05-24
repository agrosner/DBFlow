package com.raizlabs.dbflow5.rx2.query

import com.raizlabs.dbflow5.BaseUnitTest
import com.raizlabs.dbflow5.config.databaseForTable
import com.raizlabs.dbflow5.models.SimpleModel
import com.raizlabs.dbflow5.models.SimpleModel_Table
import com.raizlabs.dbflow5.query.insert
import com.raizlabs.dbflow5.query.requireResult
import com.raizlabs.dbflow5.query.select
import com.raizlabs.dbflow5.reactivestreams.query.queryStreamResults
import com.raizlabs.dbflow5.reactivestreams.transaction.asFlowable
import com.raizlabs.dbflow5.structure.delete
import com.raizlabs.dbflow5.structure.insert
import com.raizlabs.dbflow5.structure.save
import org.junit.Assert.assertEquals
import org.junit.Test

class CursorResultSubscriberTest : BaseUnitTest() {

    @Test
    fun testCanQueryStreamResults() {
        databaseForTable<SimpleModel> {
            (0..9).forEach { SimpleModel("$it").save() }

            var count = 0
            (select from SimpleModel::class)
                .queryStreamResults(this)
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
            .asFlowable { databaseWrapper, modelQueriable -> modelQueriable.queryList(databaseWrapper) }
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
            .executeInsert(databaseForTable<SimpleModel>())
        insert(SimpleModel::class, SimpleModel_Table.name)
            .values("test1")
            .executeInsert(databaseForTable<SimpleModel>())
        insert(SimpleModel::class, SimpleModel_Table.name)
            .values("test2")
            .executeInsert(databaseForTable<SimpleModel>())


        assertEquals(3, curList.size)

        val model = (select
            from SimpleModel::class
            where SimpleModel_Table.name.eq("test")).requireResult
        model.delete()

        assertEquals(2, curList.size)

        assertEquals(5, count) // once for subscription, 4 for operations
    }

}