package com.raizlabs.android.dbflow.list

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.SimpleModel
import com.raizlabs.android.dbflow.kotlinextensions.*
import com.raizlabs.android.dbflow.structure.cache.SimpleMapCache
import org.junit.Assert.*
import org.junit.Test

/**
 * Description:
 */
class FlowCursorListTest : BaseUnitTest() {


    @Test
    fun validateBuilder() {

        val list = FlowCursorList.Builder<SimpleModel>(select from SimpleModel::class)
            .modelCache(SimpleMapCache<SimpleModel>(55))
            .build()

        assertTrue(list.modelCache() is SimpleMapCache<*>)
        assertTrue(list.cachingEnabled())
    }

    @Test
    fun validateNonCachedBuilder() {

        val list = FlowCursorList.Builder<SimpleModel>(select from SimpleModel::class)
            .cacheModels(false)
            .build()

        assertFalse(list.cachingEnabled())
    }

    @Test
    fun validateCursorPassed() {
        val cursor = (select from SimpleModel::class).cursor
        val list = FlowCursorList.Builder<SimpleModel>(SimpleModel::class.java)
            .cursor(cursor)
            .build()

        assertEquals(cursor, list.cursor())
    }

    @Test
    fun validateModelQueriable() {
        val modelQueriable = (select from SimpleModel::class)
        val list = FlowCursorList.Builder<SimpleModel>(SimpleModel::class.java)
            .modelQueriable(modelQueriable)
            .build()

        assertEquals(modelQueriable, list.modelQueriable())
    }

    @Test
    fun validateSpecialModelCache() {
        (0..9).forEach {
            SimpleModel("$it").save()
        }

        val list = (select from SimpleModel::class).cursorList()
        assertEquals(10, list.count)
        val firsItem = list[0]
        assertEquals(firsItem, firsItem)
        assertEquals(list[2], list[2])

        list.clearCache()
        assertNotEquals(firsItem, list[0])
    }

    @Test
    fun validateGetAll() {
        (0..9).forEach {
            SimpleModel("$it").save()
        }

        val list = (select from SimpleModel::class).cursorList()
        val all = list.all
        assertEquals(list.count, all.size.toLong())
        all.indices.forEach {
            assertEquals(all[it], list[it])
        }
    }

    @Test
    fun validateCursorChange() {
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

