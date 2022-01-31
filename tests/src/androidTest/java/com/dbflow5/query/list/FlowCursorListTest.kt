package com.dbflow5.query.list

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.models.SimpleModel
import com.dbflow5.query.select
import com.dbflow5.structure.save
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Description:
 */
class FlowCursorListTest : BaseUnitTest() {

    @Test
    fun validateCursorPassed() {
        database<TestDatabase> {
            val cursor = (select from SimpleModel::class).cursor(this.db)
            val list = FlowCursorList.Builder(select from SimpleModel::class, this.db)
                .cursor(cursor)
                .build()

            assertEquals(cursor, list.cursor)
        }
    }

    @Test
    fun validateModelQueriable() {
        database<TestDatabase> {
            val modelQueriable = (select from SimpleModel::class)
            val list = FlowCursorList.Builder(modelQueriable, this.db)
                .build()

            assertEquals(modelQueriable, list.modelQueriable)
        }
    }

    @Test
    fun validateGetAll() {
        database<TestDatabase> {
            (0..9).forEach {
                SimpleModel("$it").save(this.db)
            }

            val list = (select from SimpleModel::class).cursorList(this.db)
            val all = list.all
            assertEquals(list.count, all.size.toLong())
        }
    }

    @Test
    fun validateCursorChange() {
        database<TestDatabase> {
            (0..9).forEach {
                SimpleModel("$it").save(this.db)
            }

            val list = (select from SimpleModel::class).cursorList(this.db)

            var cursorListFound: FlowCursorList<SimpleModel>? = null
            var count = 0
            val listener: (FlowCursorList<SimpleModel>) -> Unit = { loadedList ->
                cursorListFound = loadedList
                count++
            }
            list.addOnCursorRefreshListener(listener)
            assertEquals(10, list.count)
            SimpleModel("10").save(this.db)
            list.refresh()
            assertEquals(11, list.count)

            assertEquals(list, cursorListFound)

            list.removeOnCursorRefreshListener(listener)

            list.refresh()
            assertEquals(1, count)
        }
    }
}

