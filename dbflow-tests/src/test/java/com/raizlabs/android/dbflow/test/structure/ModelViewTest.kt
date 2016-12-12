package com.raizlabs.android.dbflow.test.structure

import com.raizlabs.android.dbflow.list.FlowCursorList
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.sql.language.Select
import com.raizlabs.android.dbflow.test.FlowTestCase

import org.junit.Test

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

/**
 * Description:
 */
class ModelViewTest : FlowTestCase() {

    /**
     * Tests to ensure the model view operates as expected
     */
    @Test
    fun testModelView() {
        var testModel2 = TestModel2()
        testModel2.order = 6
        testModel2.name = "View"
        testModel2.save()

        testModel2 = TestModel2()
        testModel2.order = 5
        testModel2.name = "View2"
        testModel2.save()

        val testModelViews = Select().from(TestModelView::class.java).queryList()
        assertTrue(!testModelViews.isEmpty())
        assertTrue(testModelViews.size == 1)

        val list = FlowCursorList.Builder(
                SQLite.select().from(TestModelView::class.java))
                .build()
        assertNotNull(list.getItem(0))
    }

}
