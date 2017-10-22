package com.raizlabs.android.dbflow.list

import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.select
import com.raizlabs.android.dbflow.models.SimpleModel
import com.raizlabs.android.dbflow.structure.cache.SimpleMapCache
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction
import org.junit.Assert.*
import org.junit.Test

class FlowQueryListTest : BaseUnitTest() {

    @Test
    fun validateBuilder() {

        val list = FlowQueryList.Builder<SimpleModel>(select from SimpleModel::class)
                .modelCache(SimpleMapCache<SimpleModel>(55))
                .transact(true)
                .changeInTransaction(true)
                .build()

        assertTrue(list.transact())
        assertTrue(list.changeInTransaction())
    }

    @Test
    fun validateListOperations() {
        val mockSuccess = mock<Transaction.Success>()
        val mockError = mock<Transaction.Error>()
        val list = (select from SimpleModel::class).flowQueryList()
                .newBuilder().success(mockSuccess)
                .error(mockError)
                .build()
        list += SimpleModel("1")

        // verify added
        assertEquals(1, list.count)
        assertFalse(list.isEmpty())

        // verify success called
        verify(mockSuccess).onSuccess(argumentCaptor<Transaction>().capture())

        list -= SimpleModel("1")
        assertEquals(0, list.count)

        list += SimpleModel("1")
        list.removeAt(0)
        assertEquals(0, list.count)

        val elements = arrayListOf(SimpleModel("1"), SimpleModel("2"))
        list.addAll(elements)
        assertEquals(2, list.count)
        list.removeAll(elements)
        assertEquals(0, list.count)

        list.addAll(elements)

        val typedArray = list.toTypedArray()
        assertEquals(typedArray.size, list.size)

        list.clear()
        assertEquals(0, list.size)
    }

}