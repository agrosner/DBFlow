package com.raizlabs.android.dbflow.container

import com.raizlabs.android.dbflow.sql.language.Delete
import com.raizlabs.android.dbflow.sql.language.Select
import com.raizlabs.android.dbflow.FlowTestCase
import org.junit.Assert.*
import org.junit.Test


class ForeignKeyModelTest : FlowTestCase() {

    @Test
    fun testForeignKeyModel() {

        Delete.tables(ForeignInteractionModel::class.java, ParentModel::class.java)

        var foreignInteractionModel = ForeignInteractionModel()
        val parentModel = ParentModel()
        parentModel.name = "Test"
        parentModel.type = "Type"
        parentModel.save()
        assertTrue(parentModel.exists())

        foreignInteractionModel.testModel1 = parentModel
        foreignInteractionModel.name = "Test2"
        foreignInteractionModel.save()

        assertTrue(foreignInteractionModel.exists())

        assertTrue(foreignInteractionModel.testModel1!!.exists())

        foreignInteractionModel = Select().from(ForeignInteractionModel::class.java)
            .where(ForeignInteractionModel_Table.name.`is`("Test2")).querySingle()
            ?: ForeignInteractionModel()
        assertNotNull(foreignInteractionModel)
        assertNotNull(foreignInteractionModel.testModel1)
        val testModel11 = foreignInteractionModel.testModel1
        assertNotNull(parentModel)
        assertEquals("Test", testModel11!!.name)

        Delete.tables(ForeignInteractionModel::class.java, ParentModel::class.java)
    }
}
