package com.raizlabs.android.dbflow.structure

import android.content.ContentValues
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.sql.language.Delete
import com.raizlabs.android.dbflow.sql.language.Select
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement
import com.raizlabs.android.dbflow.structure.listener.ContentValuesListener
import com.raizlabs.android.dbflow.structure.listener.LoadFromCursorListener
import com.raizlabs.android.dbflow.structure.listener.SQLiteStatementListener
import com.raizlabs.android.dbflow.FlowTestCase
import com.raizlabs.android.dbflow.structure.ListenerModel_Table.name
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ListenerModelTest : FlowTestCase() {

    @Test
    fun testListeners() {
        Delete.table(ListenerModel::class.java)

        val listenerModel = ListenerModel()
        listenerModel.name = "This is a test"
        val called = booleanArrayOf(false, false, false)
        listenerModel.registerListeners(
            object : SQLiteStatementListener {
                override fun onBindToStatement(databaseStatement: DatabaseStatement) {
                    called[1] = true
                }

                override fun onBindToInsertStatement(databaseStatement: DatabaseStatement) {
                    called[1] = true
                }
            },
            object : ContentValuesListener {
                override fun onBindToContentValues(contentValues: ContentValues) {
                    called[2] = true
                }

                override fun onBindToInsertValues(contentValues: ContentValues) {
                    called[2] = true
                }
            })
        listenerModel.registerLoadFromCursorListener(LoadFromCursorListener { called[0] = true })
        listenerModel.insert()
        listenerModel.update()

        val modelModelAdapter = FlowManager.getModelAdapter(ListenerModel::class.java)
        val cursor = Select().from(ListenerModel::class.java).where(name.`is`("This is a test")).query()
        assertNotNull(cursor)

        assertTrue(cursor!!.moveToFirst())
        modelModelAdapter.loadFromCursor(cursor, listenerModel)

        listenerModel.delete()
        cursor.close()

        for (call in called) {
            assertTrue(call)
        }
    }
}
