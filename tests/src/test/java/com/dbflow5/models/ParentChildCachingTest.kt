package com.dbflow5.models

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.query.result
import com.dbflow5.query.select
import com.dbflow5.structure.load
import com.dbflow5.structure.save
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Description:
 */
class ParentChildCachingTest : BaseUnitTest() {


    @Test
    fun testCanLoadChildFromCache() = database(TestDatabase::class) {
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
        var parentChild = parent.child!!
        parentChild = parentChild.load()!!

        assertEquals(1, parentChild.id)
        assertEquals("Test child", parentChild.name)
    }
}