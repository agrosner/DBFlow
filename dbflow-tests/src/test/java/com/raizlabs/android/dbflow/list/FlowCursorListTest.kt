package com.raizlabs.android.dbflow.list

import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.list.FlowCursorList
import com.raizlabs.android.dbflow.sql.language.Delete
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.structure.cache.ModelLruCache
import com.raizlabs.android.dbflow.structure.database.transaction.FastStoreModelTransaction
import com.raizlabs.android.dbflow.FlowTestCase
import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.querymodel.TestQueryModel
import com.raizlabs.android.dbflow.structure.TestModel1
import com.raizlabs.android.dbflow.structure.TestModel1_Table

import org.junit.Test

import java.util.ArrayList

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

/**
 * Description:
 */
class FlowCursorListTest : FlowTestCase() {


    @Test
    fun test_validateBuilderCacheable() {
        val cursorList = FlowCursorList.Builder(TestModel1::class.java)
                .cacheSize(50)
                .modelCache(ModelLruCache.newInstance<TestModel1>(50))
                .modelQueriable(SQLite.select().from(TestModel1::class.java))
                .build()

        assertEquals(TestModel1::class.java, cursorList.table())
        assertEquals(50, cursorList.cacheSize().toLong())
        assertTrue(cursorList.modelCache() is ModelLruCache<*>)
        assertEquals(true, cursorList.cachingEnabled())
        assertNotNull(cursorList.cursor())

        cursorList.close()
    }

    @Test
    fun test_validateBuilderCustomQuery() {
        val cursorList = FlowCursorList.Builder(TestQueryModel::class.java)
                .cursor(SQLite
                        .select(TestModel1_Table.name.`as`("newName"))
                        .from(TestModel1::class.java).query())
                .build()

        assertEquals(TestQueryModel::class.java, cursorList.table())
        assertTrue(cursorList.cachingEnabled())
        assertEquals(50, cursorList.cacheSize().toLong())

        cursorList.close()
    }

    @Test
    fun test_ensureModelCache() {
        Delete.table(TestModel1::class.java)

        val model = TestModel1()
        model.name = "Test"
        model.save()

        val list = FlowCursorList.Builder(TestModel1::class.java)
                .cacheModels(true)
                .modelQueriable(SQLite.select().from(TestModel1::class.java))
                .build()
        assertEquals(1, list.count.toLong())
        assertEquals(model.name, list.getItem(0).name)
        assertEquals(list.getItem(0), list.getItem(0))

        list.close()
    }

    @Test
    fun test_canIterateCursor() {
        val models = ArrayList<TestModel1>()
        for (i in 0..49) {
            val model = TestModel1()
            model.name = "" + i
            models.add(model)
        }
        FlowManager.getDatabase(TestDatabase::class.java)
                .executeTransaction(FastStoreModelTransaction
                        .insertBuilder(FlowManager.getModelAdapter(TestModel1::class.java))
                        .addAll(models)
                        .build())

        val cursorList = SQLite.select()
                .from(TestModel1::class.java).cursorList()
        assertNotEquals(0, cursorList.count.toLong())
        assertEquals(50, cursorList.count.toLong())
        var count = 0
        for (model in cursorList) {
            assertEquals(count.toString() + "", model.name)
            count++
        }

    }
}
