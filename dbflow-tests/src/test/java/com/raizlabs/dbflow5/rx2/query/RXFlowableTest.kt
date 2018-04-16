package com.raizlabs.dbflow5.rx2.query

import com.raizlabs.dbflow5.BaseUnitTest
import com.raizlabs.dbflow5.models.Author
import com.raizlabs.dbflow5.models.Author_Table
import com.raizlabs.dbflow5.models.Blog
import com.raizlabs.dbflow5.models.Blog_Table
import com.raizlabs.dbflow5.models.SimpleModel
import com.raizlabs.dbflow5.models.SimpleModel_Table
import com.raizlabs.dbflow5.query.cast
import com.raizlabs.dbflow5.query.select
import com.raizlabs.dbflow5.rx2.transaction.asFlowable
import com.raizlabs.dbflow5.structure.save
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Description:
 */
class RXFlowableTest : BaseUnitTest() {

    @Test
    fun testCanObserveChanges() {

        (0..100).forEach { SimpleModel("$it").save() }

        var list = mutableListOf<SimpleModel>()
        var triggerCount = 0
        val subscription = (select from SimpleModel::class
                where cast(SimpleModel_Table.name).asInteger().greaterThan(50))
                .asFlowable { db, modelQueriable -> modelQueriable.queryList(db) }
                .subscribe {
                    list = it
                    triggerCount += 1
                }

        assertEquals(50, list.size)
        subscription.dispose()

        SimpleModel("should not trigger").save()
        assertEquals(1, triggerCount)

    }

    @Test
    fun testObservesJoinTables() {
        val joinOn = Blog_Table.name.withTable()
                .eq(Author_Table.first_name.withTable() + " " + Author_Table.last_name.withTable())
        assertEquals("`Blog`.`name`=`Author`.`first_name`+' '+`Author`.`last_name`", joinOn.query)

        var list = mutableListOf<Blog>()
        var calls = 0
        (select from Blog::class
                innerJoin Author::class
                on joinOn)
                .asFlowable { db, modelQueriable -> modelQueriable.queryList(db) }
                .subscribe {
                    calls++
                    list = it
                }

        val authors = (0 until 10).map { Author(it, firstName = "${it}name", lastName = "${it}last") }
        (0 until 10).forEach { Blog(it, name = "${it}name ${it}last", author = authors[it]).save() }

        assertEquals(21, calls) // 1 for initial, 10 for each model object
        assertEquals(10, list.size)
    }
}