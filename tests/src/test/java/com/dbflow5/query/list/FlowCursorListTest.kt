package com.dbflow5.query.list

import com.dbflow5.BaseUnitTest
import com.dbflow5.config.databaseForTable
import com.dbflow5.models.SimpleModel
import com.dbflow5.query.cursor
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
        databaseForTable<SimpleModel> { dbFlowDatabase ->
            val cursor = (select from SimpleModel::class).cursor
            val list = FlowCursorList.Builder(select from SimpleModel::class, dbFlowDatabase)
                    .cursor(cursor)
                    .build()

            assertEquals(cursor, list.cursor)
        }
    }

    @Test
    fun validateModelQueriable() {
        databaseForTable<SimpleModel> { dbFlowDatabase ->
            val modelQueriable = (select from SimpleModel::class)
            val list = FlowCursorList.Builder(modelQueriable, dbFlowDatabase)
                    .build()

            assertEquals(modelQueriable, list.modelQueriable)
        }
    }

    @Test
    fun validateGetAll() {
        databaseForTable<SimpleModel> { dbFlowDatabase ->
            (0..9).forEach {
                SimpleModel("$it").save()
            }

            val list = (select from SimpleModel::class).cursorList(dbFlowDatabase)
            val all = list.all
            assertEquals(list.count, all.size.toLong())
        }
    }

    @Test
    fun validateCursorChange() {
        databaseForTable<SimpleModel> { db ->
            (0..9).forEach {
                SimpleModel("$it").save(db)
            }

            val list = (select from SimpleModel::class).cursorList(db)

            var cursorListFound: FlowCursorList<SimpleModel>? = null
            var count = 0
            val listener: (FlowCursorList<SimpleModel>) -> Unit = { loadedList ->
                cursorListFound = loadedList
                count++
            }
            list.addOnCursorRefreshListener(listener)
            assertEquals(10, list.count)
            SimpleModel("10").save(db)
            list.refresh()
            assertEquals(11, list.count)

            assertEquals(list, cursorListFound)

            list.removeOnCursorRefreshListener(listener)

            list.refresh()
            assertEquals(1, count)
        }
    }
}

