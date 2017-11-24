package com.raizlabs.android.dbflow.models

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.config.writableDatabase
import com.raizlabs.android.dbflow.sql.language.select
import com.raizlabs.android.dbflow.sql.queriable.result
import com.raizlabs.android.dbflow.structure.load
import com.raizlabs.android.dbflow.structure.save
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Description:
 */
class ParentChildCachingTest : BaseUnitTest() {


    @Test
    fun testCanLoadChildFromCache() = writableDatabase(TestDatabase::class) {
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