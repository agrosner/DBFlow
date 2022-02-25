package com.dbflow5.rx2.query

import com.dbflow5.TestDatabase_Database
import com.dbflow5.config.writableTransaction
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.query.delete
import com.dbflow5.query.insert
import com.dbflow5.query.select
import com.dbflow5.reactivestreams.transaction.asFlowable
import com.dbflow5.simpleModelAdapter
import com.dbflow5.test.DatabaseTestRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertTrue

class CursorResultSubscriberTest {

    @get:Rule
    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun testCanObserveOnTableChangesWithModelOps() = dbRule.runBlockingTest {
        var count = 0
        (select from simpleModelAdapter)
            .asFlowable(db) { list() }
            .subscribe {
                count++
            }
        val model = SimpleModel("test")
        db.writableTransaction {
            simpleModelAdapter.save(model)
            simpleModelAdapter.delete(model)
            simpleModelAdapter.insert(model)
        }
        assertEquals(2, count) // once for subscription, 1 for operations in transaction.
    }

    @Test
    fun testCanObserveOnTableChangesWithTableOps() = dbRule.runBlockingTest {
        simpleModelAdapter.delete().execute()
        var count = 0
        var curList: List<SimpleModel> = arrayListOf()
        (select from db.simpleModelAdapter)
            .asFlowable(db) { list() }
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

        val model = (select
            from simpleModelAdapter
            where SimpleModel_Table.name.eq("test"))
            .single()

        simpleModelAdapter.delete(model)
        db.tableObserver.checkForTableUpdates()

        assertEquals(2, curList.size)
        assertEquals(3, count) // once for subscription, 2 for transactions
    }
}