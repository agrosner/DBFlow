package com.raizlabs.android.dbflow.test.list

import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.list.FlowQueryList
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.structure.database.transaction.FastStoreModelTransaction
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction
import com.raizlabs.android.dbflow.test.FlowTestCase
import com.raizlabs.android.dbflow.test.TestDatabase
import com.raizlabs.android.dbflow.test.structure.TestModel1

import org.junit.Test

import java.util.ArrayList

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue

/**
 * Description:
 */
class FlowQueryListTest : FlowTestCase() {


    @Test
    fun test_ensureValidateBuilder() {
        val success = Transaction.Success { }
        val error = Transaction.Error { transaction, error -> }
        val queryList = FlowQueryList.Builder(TestModel1::class.java)
                .success(success)
                .error(error)
                .cacheSize(50)
                .changeInTransaction(true)
                .build()
        assertEquals(success, queryList.success())
        assertEquals(error, queryList.error())
        assertEquals(true, queryList.cursorList().cachingEnabled())
        assertEquals(50, queryList.cursorList().cacheSize().toLong())
        assertTrue(queryList.changeInTransaction())

    }

    @Test
    fun test_canIterateQueryList() {
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

        val queryList = SQLite.select()
                .from(TestModel1::class.java).flowQueryList()
        assertNotEquals(0, queryList.size.toLong())
        assertEquals(50, queryList.size.toLong())
        var count = 0
        for (model in queryList) {
            assertEquals(count.toString() + "", model.name)
            count++
        }
    }
}
