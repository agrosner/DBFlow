package com.raizlabs.android.dbflow.test.order

import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.kotlinextensions.delete
import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.list
import com.raizlabs.android.dbflow.kotlinextensions.select
import com.raizlabs.android.dbflow.structure.database.transaction.FastStoreModelTransaction
import com.raizlabs.android.dbflow.test.FlowTestCase
import com.raizlabs.android.dbflow.test.TestDatabase
import com.raizlabs.android.dbflow.test.structure.TestModel1
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Description:
 */
class OrderItemTest : FlowTestCase() {

    @Test
    fun ensureCanSave() {
        delete<OrderItem>().execute()
        val testModel = TestModel1().apply { name = "Name1"; insert() }
        val testModel2 = TestModel1().apply { name = "Name2"; insert() }
        val list = arrayListOf(
                OrderItem(1, testModel),
                OrderItem(2, testModel2),
                OrderItem(3, null),
                OrderItem(4, null),
                OrderItem(5, null))
        FlowManager.getDatabase(TestDatabase::class.java).executeTransaction(
                FastStoreModelTransaction
                        .saveBuilder(FlowManager.getModelAdapter(OrderItem::class.java))
                        .addAll(list)
                        .build())

        val loaded = (select from OrderItem::class).list
        assertEquals(1, loaded[0].id)
        assertEquals(testModel.name, loaded[0].product?.name)
        assertEquals(2, loaded[1].id)
        assertEquals(testModel2.name, loaded[1].product?.name)
        (2..4).forEach { assertNull(loaded[it].product) }
    }
}