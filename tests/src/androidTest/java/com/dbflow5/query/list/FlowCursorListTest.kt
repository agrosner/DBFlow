package com.dbflow5.query.list

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.config.readableTransaction
import com.dbflow5.config.writableTransaction
import com.dbflow5.models.SimpleModel
import com.dbflow5.query.select
import com.dbflow5.simpleModel
import com.dbflow5.structure.save
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Description:
 */
class FlowCursorListTest : BaseUnitTest() {

    @Test
    fun validateCursorPassed() = runBlockingTest {
        database<TestDatabase>().readableTransaction {
            val cursor = (select from SimpleModel::class).cursor()
            val list = FlowCursorList.Builder(select from SimpleModel::class, this.db)
                .cursor(cursor)
                .build()

            assertEquals(cursor, list.cursor)
        }
    }

    @Test
    fun validateModelQueriable() = runBlockingTest {
        database<TestDatabase>().writableTransaction {
            val modelQueriable = (select from SimpleModel::class)
            val list = FlowCursorList.Builder(modelQueriable, this.db)
                .build()

            assertEquals(modelQueriable, list.modelQueriable)
        }
    }

    @Test
    fun validateGetAll() = runBlockingTest {
        database<TestDatabase>().writableTransaction {
            (0..9).forEach {
                simpleModel.save(SimpleModel("$it"))
            }

            val list = (select from SimpleModel::class).cursorList(this.db)
            val all = list.all
            assertEquals(list.count, all.size.toLong())
        }
    }

    @Test
    fun validateCursorChange() = runBlockingTest {
        database<TestDatabase>().writableTransaction {
            (0..9).forEach {
                simpleModel.save(SimpleModel("$it"))
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

