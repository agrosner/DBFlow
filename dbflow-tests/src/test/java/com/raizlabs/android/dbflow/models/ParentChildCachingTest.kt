package com.raizlabs.android.dbflow.models

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.sql.language.from
import com.raizlabs.android.dbflow.kotlinextensions.result
import com.raizlabs.android.dbflow.sql.language.select
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Description:
 */
class ParentChildCachingTest : BaseUnitTest() {


    @Test
    fun testCanLoadChildFromCache() {
        val child = TestModelChild()
        child.id = 1
        child.name = "Test child"
        child.save()

        var parent = TestModelParent()
        parent.id = 1
        parent.name = "Test parent"
        parent.child = child
        parent.save()

        parent = (select from TestModelParent::class).result!!
        val parentChild = parent.child!!
        parentChild.load()

        assertEquals(1, parentChild.id)
        assertEquals("Test child", parentChild.name)
    }
}