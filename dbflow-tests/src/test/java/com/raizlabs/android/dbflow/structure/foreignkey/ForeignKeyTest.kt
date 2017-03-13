package com.raizlabs.android.dbflow.structure.foreignkey

import com.raizlabs.android.dbflow.sql.language.Select
import com.raizlabs.android.dbflow.FlowTestCase
import com.raizlabs.android.dbflow.structure.autoincrement.TestModelAI

import org.junit.Test

import com.raizlabs.android.dbflow.structure.foreignkey.ForeignModel_Table.name
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

class ForeignKeyTest : FlowTestCase() {

    // region Test Foreign Key

    @Test
    fun testForeignKey() {
        val parentModel = ForeignParentModel()
        parentModel.name = "Test"
        parentModel.save()

        val foreignModel = ForeignModel()
        foreignModel.testModel1 = parentModel
        foreignModel.name = "Test"
        foreignModel.save()

        val retrieved = Select().from(ForeignModel::class.java)
                .where(name.`is`("Test"))
                .querySingle()
        assertNotNull(retrieved)
        assertNotNull(retrieved!!.testModel1)
        assertEquals(retrieved.testModel1, foreignModel.testModel1)
    }

    @Test
    fun testForeignKey2() {

        val testModelAI = TestModelAI()
        testModelAI.name = "TestAI"
        testModelAI.save()

        val foreignModel2 = ForeignModel2()
        foreignModel2.testModelAI = testModelAI
        foreignModel2.name = "Test"
        foreignModel2.save()

        val retrieved = Select().from(ForeignModel2::class.java)
                .where(ForeignModel2_Table.name.`is`("Test"))
                .querySingle()
        assertNotNull(retrieved)
        assertNotNull(retrieved!!.testModelAI)
        assertEquals(retrieved.testModelAI, foreignModel2.testModelAI)
    }

    // endregion Test Foreign Key
}
