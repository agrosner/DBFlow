package com.raizlabs.android.dbflow.sql.fast

import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.sql.language.Delete
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.structure.database.transaction.FastStoreModelTransaction
import com.raizlabs.android.dbflow.FlowTestCase
import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.contentobserver.ContentObserverModel

import org.junit.Test

import java.util.ArrayList
import java.util.Date
import java.util.UUID

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull

/**
 * Description:
 */
class FastModelTest : FlowTestCase() {

    @Test
    fun test_canLoadQuery() {
        Delete.table(FastModel::class.java)

        var testModel1s = getRandomFastModels(100, FastModel::class.java)

        FlowManager.getDatabase(TestDatabase::class.java)
                .executeTransaction(FastStoreModelTransaction
                        .insertBuilder(FlowManager.getModelAdapter(FastModel::class.java))
                        .addAll(testModel1s).build())

        testModel1s = SQLite.select()
                .from(FastModel::class.java).queryList()
        for (i in testModel1s.indices) {
            val fastModel = testModel1s[i]
            assertEquals(i.toLong(), fastModel.id)
            assertNotNull(fastModel.contentObserverModel)
        }

        Delete.table(FastModel::class.java)
    }

    @Test
    fun test_speed() {

        // dont pollute results with initialization
        SQLite.select().from(NonFastModel::class.java).queryList()
        SQLite.select().from(FastModel::class.java).queryList()

        var nonFastList = getRandomFastModels(5000, NonFastModel::class.java)
        FlowManager.getDatabase(TestDatabase::class.java)
                .executeTransaction(FastStoreModelTransaction
                        .insertBuilder(FlowManager.getModelAdapter(NonFastModel::class.java))
                        .addAll(nonFastList).build())

        var startTime = System.currentTimeMillis()

        nonFastList = SQLite.select().from(NonFastModel::class.java).queryList()

        println("Loading for less fast took: " + (System.currentTimeMillis() - startTime))

        Delete.tables(NonFastModel::class.java, ContentObserverModel::class.java)

        var list = getRandomFastModels(5000, FastModel::class.java)
        FlowManager.getDatabase(TestDatabase::class.java)
                .executeTransaction(FastStoreModelTransaction
                        .insertBuilder(FlowManager.getModelAdapter(FastModel::class.java))
                        .addAll(list).build())

        startTime = System.currentTimeMillis()
        list = SQLite.select().from(FastModel::class.java).queryList()
        println("Loading for fast took: " + (System.currentTimeMillis() - startTime))

        startTime = System.currentTimeMillis()
        list = SQLite.select().from(FastModel::class.java).flowQueryList()
        println("Loading for query list took: " + (System.currentTimeMillis() - startTime))

    }

    private fun <T : FastModel> getRandomFastModels(count: Int, fastModelClass: Class<T>): List<T> {
        val testModel1s = ArrayList<T>()
        var testModel1: T
        for (i in 0..count - 1) {
            testModel1 = FlowManager.getModelAdapter(fastModelClass).newInstance()
            testModel1.name = UUID.randomUUID().toString()
            testModel1.id = i.toLong()
            testModel1.date = Date(System.currentTimeMillis())
            val contentObserverModel = ContentObserverModel()
            contentObserverModel.name = UUID.randomUUID().toString()
            contentObserverModel.id = i
            contentObserverModel.insert()
            testModel1.contentObserverModel = contentObserverModel
            testModel1s.add(testModel1)
        }
        return testModel1s
    }
}
