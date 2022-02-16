package com.dbflow5.rx2.query

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.blogAdapter
import com.dbflow5.config.database
import com.dbflow5.config.readableTransaction
import com.dbflow5.config.writableTransaction
import com.dbflow5.models.Author
import com.dbflow5.models.Author_Table
import com.dbflow5.models.Blog
import com.dbflow5.models.Blog_Table
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.query2.leftOuterJoin
import com.dbflow5.query2.operations.StandardMethods
import com.dbflow5.query2.select
import com.dbflow5.reactivestreams.transaction.asFlowable
import com.dbflow5.simpleModelAdapter
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Description:
 */
class RXFlowableTest : BaseUnitTest() {

    @Test
    fun testCanObserveChanges() = runBlockingTest {
        val database = database<TestDatabase>()
        database.writableTransaction {
            simpleModelAdapter.saveAll((0..100).map {
                SimpleModel("$it")
            })
        }

        var list = listOf<SimpleModel>()
        var triggerCount = 0
        val subscription = database.readableTransaction {
            (select from simpleModelAdapter
                where StandardMethods.Cast(SimpleModel_Table.name).asInteger().greaterThan(50))
                .asFlowable(database) { list() }
                .subscribe {
                    list = it
                    triggerCount += 1
                }
        }

        assertEquals(50, list.size)
        subscription.dispose()

        database.writableTransaction {
            simpleModelAdapter.save(SimpleModel("should not trigger"))
        }
        assertEquals(1, triggerCount)
    }

    @Test
    fun testObservesJoinTables() = runBlockingTest {
        database<TestDatabase> { db ->
            val joinOn = Blog_Table.name.withTable()
                .eq(Author_Table.first_name.withTable() + " " + Author_Table.last_name.withTable())
            assertEquals(
                "`Blog`.`name` = (`Author`.`first_name` + ' ' + `Author`.`last_name`)",
                joinOn.query
            )

            var list = listOf<Blog>()
            var calls = 0
            (select from db.blogAdapter
                leftOuterJoin db.authorAdapter
                on joinOn)
                .asFlowable(db) { list() }
                .subscribe {
                    calls++
                    list = it
                }

            val authors =
                (1 until 11).map { Author(it, firstName = "${it}name", lastName = "${it}last") }
            db.writableTransaction {
                (1 until 11).forEach {
                    blogAdapter.save(
                        Blog(
                            it,
                            name = "${it}name ${it}last",
                            author = authors[it - 1]
                        )
                    )
                }
            }

            assertEquals(2, calls) // 1 for initial, 1 for batch of changes
            assertEquals(10, list.size)
        }
    }
}