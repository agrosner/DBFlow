package com.raizlabs.android.dbflow.structure.autoincrement

import com.raizlabs.android.dbflow.sql.language.Delete
import com.raizlabs.android.dbflow.sql.language.Select
import com.raizlabs.android.dbflow.FlowTestCase
import com.raizlabs.android.dbflow.structure.TestModel1

import org.junit.Test

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class ModelAutoIncrementTest : FlowTestCase() {

    @Test
    fun testModelAutoIncrement() {
        Delete.table(TestModel1::class.java)

        val testModelAI = TestModelAI()
        testModelAI.name = "Test"
        testModelAI.insert()

        assertTrue(testModelAI.exists())

        testModelAI.insert()
        testModelAI.insert()

        val testModelAI2 = TestModelAI()
        testModelAI2.id = testModelAI.id
        testModelAI2.name = "Test2"
        testModelAI2.update()

        val testModelAI3 = Select().from(TestModelAI::class.java)
                .where(TestModelAI_Table.id.`is`(testModelAI.id))
                .querySingle()
        assertEquals(testModelAI3!!.name, testModelAI2.name)

        testModelAI.delete()
        assertTrue(!testModelAI.exists())

        Delete.table(TestModel1::class.java)
    }

    @Test
    fun test_singleFieldAutoIncrement() {

        Delete.table(TestSingleFieldAutoIncrement::class.java)

        val singleFieldAutoIncrement = TestSingleFieldAutoIncrement()
        singleFieldAutoIncrement.insert()

        assertTrue(singleFieldAutoIncrement.id > 0)

        Delete.table(TestSingleFieldAutoIncrement::class.java)
    }
}
