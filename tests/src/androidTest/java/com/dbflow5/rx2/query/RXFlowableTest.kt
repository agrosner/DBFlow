package com.dbflow5.rx2.query

import com.dbflow5.TestDatabase_Database
import com.dbflow5.blogAdapter
import com.dbflow5.models.Author
import com.dbflow5.models.Author_Table
import com.dbflow5.models.Blog
import com.dbflow5.models.Blog_Table
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.query.leftOuterJoin
import com.dbflow5.query.methods.cast
import com.dbflow5.query.select
import com.dbflow5.reactivestreams.transaction.asFlowable
import com.dbflow5.simpleModelAdapter
import com.dbflow5.test.DatabaseTestRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import kotlin.test.Test

/**
 * Description:
 */
class RXFlowableTest {

    
    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun testCanObserveChanges() = dbRule.runTest {
        simpleModelAdapter.saveAll((0..100).map {
            SimpleModel("$it")
        })

        var list = listOf<SimpleModel>()
        var triggerCount = 0
        val subscription = (select from simpleModelAdapter
            where cast(SimpleModel_Table.name).asInteger().greaterThan(50))
            .asFlowable(db) { list() }
            .subscribe {
                list = it
                triggerCount += 1
            }

        assertEquals(50, list.size)
        subscription.dispose()
        simpleModelAdapter.save(SimpleModel("should not trigger"))
        assertEquals(1, triggerCount)
    }

    @Test
    fun testObservesJoinTables() = dbRule.runTest {
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
        blogAdapter.saveAll((1 until 11).map {
            Blog(
                it,
                name = "${it}name ${it}last",
                author = authors[it - 1]
            )
        })

        assertEquals(10, list.size)
        assertEquals(2, calls) // 1 for initial, 1 for batch of changes
    }
}