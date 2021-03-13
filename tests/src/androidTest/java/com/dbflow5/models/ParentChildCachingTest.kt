package com.dbflow5.models

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.query.select
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Description:
 */
class ParentChildCachingTest : BaseUnitTest() {


    @Test
    fun testCanLoadChildFromCache() {
        database<TestDatabase> { db ->
            val child = TestModelChild()
            child.id = 1
            child.name = "Test child"
            child.save(db)

            var parent = TestModelParent()
            parent.id = 1
            parent.name = "Test parent"
            parent.child = child
            parent.save(db)

            parent = (select from TestModelParent::class).requireSingle(db)
            var parentChild = parent.child!!
            parentChild = parentChild.load(db)!!

            assertEquals(1, parentChild.id)
            assertEquals("Test child", parentChild.name)
        }
    }
}