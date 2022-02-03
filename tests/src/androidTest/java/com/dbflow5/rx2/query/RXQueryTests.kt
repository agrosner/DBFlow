package com.dbflow5.rx2.query

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.beginTransactionAsync
import com.dbflow5.config.database
import com.dbflow5.config.writableTransaction
import com.dbflow5.database.DatabaseStatement
import com.dbflow5.database.FlowCursor
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.query.insert
import com.dbflow5.query.property.Property
import com.dbflow5.query.select
import com.dbflow5.query.selectCountOf
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

            this.db.beginTransactionAsync { (select from SimpleModel::class).cursor()!! }
                .asMaybe()
                .subscribe {
                    cursor = it
                }

            assertEquals(1, cursor!!.count)
            cursor!!.close()
        }
    }

    @Test
    fun testCanCompileStatement() {
        var databaseStatement: DatabaseStatement? = null
        database<TestDatabase> { db ->
            db.beginTransactionAsync {
                insert<SimpleModel>(SimpleModel_Table.name.`is`("name")).compileStatement()
            }.asSingle()
                .subscribe { statement ->
                    databaseStatement = statement
                }
            databaseStatement!!.close()
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
                (selectCountOf(Property.ALL_PROPERTY) from SimpleModel::class).longValue()
            }
                .asSingle()
                .subscribe { value ->
                    count = value
                }

            assertEquals(2, count)
        }
    }

    @Test
    fun testInsertMethod() {
        var count = 0L
        database<TestDatabase>()
            .beginTransactionAsync {
                (insert<SimpleModel>(SimpleModel_Table.name.eq("name"))).executeInsert()
            }.asSingle()
            .subscribe { c ->
                count = c
            }

        assertEquals(1, count)
    }

}