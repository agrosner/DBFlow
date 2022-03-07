package com.dbflow5.rx2.query

import com.dbflow5.test.TestDatabase_Database
import com.dbflow5.config.beginTransactionAsync
import com.dbflow5.database.FlowCursor
import com.dbflow5.test.SimpleModel
import com.dbflow5.test.SimpleModel_Table
import com.dbflow5.query.insert
import com.dbflow5.query.operations.Literal
import com.dbflow5.query.select
import com.dbflow5.query.selectCountOf
import com.dbflow5.reactivestreams.transaction.asMaybe
import com.dbflow5.reactivestreams.transaction.asSingle
import com.dbflow5.test.DatabaseTestRule
import org.junit.Assert.assertEquals
import kotlin.test.Test

class RXQueryTests {

    
    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun testCanQuery() = dbRule.runTest {
        simpleModelAdapter.save(SimpleModel("Name"))

        var cursor: FlowCursor? = null

        this.db.beginTransactionAsync { Result.success(simpleModelAdapter.select().cursor()) }
            .asMaybe()
            .subscribe {
                cursor = it.getOrNull()
            }

        assertEquals(1, cursor!!.size)
        cursor!!.close()
    }

    @Test
    fun testCountMethod() = dbRule.runTest {
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

    @Test
    fun testInsertMethod() = dbRule {
        var count = 0L
        db.beginTransactionAsync {
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