package com.raizlabs.android.dbflow.test.list

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.list.FlowCursorList
import com.raizlabs.android.dbflow.list.FlowQueryList
import com.raizlabs.android.dbflow.sql.language.Delete
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.structure.database.transaction.FastStoreModelTransaction
import com.raizlabs.android.dbflow.test.FlowTestCase
import com.raizlabs.android.dbflow.test.utils.GenerationUtils
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Description:
 */
class ListTest : FlowTestCase() {

    lateinit var modelList: FlowQueryList<ListModel>

    @Before
    fun setupTest() {
        Delete.table(ListModel::class.java)
        modelList = FlowQueryList.Builder(ListModel::class.java)
            .modelQueriable(SQLite.select().from(ListModel::class.java))
            .build()
    }

    @After
    fun deconstructTest() {
        Delete.table(ListModel::class.java)
    }

    @Test
    fun testTableList() {

        val testModel1s = GenerationUtils.generateRandomModels(ListModel::class.java, 100)

        FlowManager.getDatabaseForTable(ListModel::class.java)
            .executeTransaction(FastStoreModelTransaction
                .saveBuilder(FlowManager.getModelAdapter(ListModel::class.java))
                .addAll(testModel1s)
                .build())

        modelList = FlowQueryList.Builder(SQLite.select().from(ListModel::class.java))
            .build()

        assertTrue(modelList.size == 100)

        assertTrue(modelList.containsAll(testModel1s))

        val model1 = modelList.removeAt(0)

        assertTrue(modelList.size == 99)

        assertTrue(modelList.add(model1))

        assertTrue(modelList.size == 100)

        modelList.set(model1)

        modelList.clear()

        assertTrue(modelList.size == 0)
    }

    @Test
    fun testTableListEmpty() {
        val listModel = ListModel()
        listModel.name = "Test"
        modelList.add(listModel)

        assertTrue(modelList.size == 1)
    }

    private inner class TestModelAdapter(private val mFlowCursorList: FlowCursorList<ListModel>) : BaseAdapter() {

        override fun getCount(): Int {
            return mFlowCursorList.count
        }

        override fun getItem(position: Int): ListModel {
            return mFlowCursorList.getItem(position.toLong())
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View, parent: ViewGroup): View? {
            return null
        }
    }

    @Test
    fun testCursorList() {

        val testModel1s = GenerationUtils.generateRandomModels(ListModel::class.java, 50)
        FlowManager.getDatabase(ListDatabase::class.java)
            .executeTransaction(FastStoreModelTransaction
                .insertBuilder(FlowManager.getModelAdapter(ListModel::class.java))
                .addAll(testModel1s)
                .build())

        val flowCursorList = FlowCursorList.Builder(ListModel::class.java)
            .cacheModels(true)
            .modelQueriable(SQLite.select()
                .from(ListModel::class.java))
            .build()

        val modelAdapter = TestModelAdapter(flowCursorList)

        assertEquals(testModel1s.size.toLong(), modelAdapter.count.toLong())
        assertEquals(flowCursorList.all.size.toLong(), testModel1s.size.toLong())

    }
}
