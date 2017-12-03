package com.raizlabs.dbflow5.query.list

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.raizlabs.dbflow5.BaseUnitTest
import com.raizlabs.dbflow5.config.databaseForTable
import com.raizlabs.dbflow5.models.SimpleModel
import com.raizlabs.dbflow5.query.cursor
import com.raizlabs.dbflow5.query.select
import com.raizlabs.dbflow5.structure.save
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Description:
 */
class FlowCursorListTest : BaseUnitTest() {

    @Test
    fun validateCursorPassed() {
        databaseForTable<SimpleModel> {
            val cursor = (select from SimpleModel::class).cursor
            val list = FlowCursorList.Builder(select from SimpleModel::class)
                    .cursor(cursor)
                    .build()

            assertEquals(cursor, list.cursor())
        }
    }

    @Test
    fun validateModelQueriable() {
        databaseForTable<SimpleModel> {
            val modelQueriable = (select from SimpleModel::class)
            val list = FlowCursorList.Builder(modelQueriable)
                    .build()

            assertEquals(modelQueriable, list.modelQueriable)
        }
    }

    @Test
    fun validateGetAll() {
        databaseForTable<SimpleModel> {
            (0..9).forEach {
                SimpleModel("$it").save()
            }

            val list = (select from SimpleModel::class).cursorList()
            val all = list.all
            assertEquals(list.count, all.size.toLong())
        }
    }

    @Test
    fun validateCursorChange() {
        databaseForTable<SimpleModel> {
            (0..9).forEach {
                SimpleModel("$it").save()
            }

            val list = (select from SimpleModel::class).cursorList()

            val listener = mock<FlowCursorList.OnCursorRefreshListener<SimpleModel>>()
            list.addOnCursorRefreshListener(listener)
            assertEquals(10, list.count)
            SimpleModel("10").save()
            list.refresh()
            assertEquals(11, list.count)

            verify(listener).onCursorRefreshed(list)

            list.removeOnCursorRefreshListener(listener)

            list.refresh()
            verify(listener, times(1)).onCursorRefreshed(list)
        }
    }
}

