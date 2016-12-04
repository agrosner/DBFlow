package com.raizlabs.android.dbflow.test.transaction

import com.raizlabs.android.dbflow.config.DatabaseDefinition
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.sql.language.CursorResult
import com.raizlabs.android.dbflow.sql.language.Delete
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.structure.ModelAdapter
import com.raizlabs.android.dbflow.structure.cache.ModelCache
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper
import com.raizlabs.android.dbflow.structure.database.transaction.FastStoreModelTransaction
import com.raizlabs.android.dbflow.structure.database.transaction.ITransaction
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction
import com.raizlabs.android.dbflow.test.FlowTestCase
import com.raizlabs.android.dbflow.test.TestDatabase
import com.raizlabs.android.dbflow.test.structure.TestModel1
import com.raizlabs.android.dbflow.test.structure.TestModel1_Table
import com.raizlabs.android.dbflow.test.structure.TestModel2
import com.raizlabs.android.dbflow.test.utils.GenerationUtils

import org.junit.Before
import org.junit.Test

import java.util.ArrayList
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue

/**
 * Description:
 */
class TransactionsTest : FlowTestCase() {

    var database: DatabaseDefinition


    @Before
    fun beforeTests() {
        database = FlowManager.getDatabase(TestDatabase::class.java)
    }

    @Test
    fun test_basicAsyncTransactionCall() {

        val called = AtomicBoolean(false)
        val transaction = MockTransaction(database.beginTransactionAsync { called.set(true) }.build(), database)
        transaction.execute()

        assertTrue(called.get())
    }

    @Test
    fun test_basicTransaction() {
        val called = AtomicBoolean(false)
        database.executeTransaction { called.set(true) }
        assertTrue(called.get())
    }

    @Test
    fun test_processTransaction() {
        val count = 10
        val testModel1List = ArrayList<TestModel1>()
        var testModel1: TestModel1
        for (i in 0..count - 1) {
            testModel1 = TestModel1()
            testModel1.name = "Name" + i
            testModel1List.add(testModel1)
        }
        val processCalled = AtomicBoolean(false)
        val modelProcessedCount = AtomicInteger(0)
        val processModelTransaction = ProcessModelTransaction.Builder(ProcessModelTransaction.ProcessModel<TestModel1> { processCalled.set(true) }).processListener(ProcessModelTransaction.OnModelProcessListener<TestModel1> { current, total, modifiedModel -> modelProcessedCount.incrementAndGet() }).addAll(testModel1List).build()
        val transaction = Transaction.Builder(processModelTransaction, database).build()
        MockTransaction(transaction, database).execute()

        assertTrue(transaction.transaction() is ProcessModelTransaction<*>)
        assertEquals(processCalled.get(), true)
        assertEquals(modelProcessedCount.get().toLong(), count.toLong())
    }

    @Test
    fun test_queryTransaction() {

        var testModel1 = TestModel1()
        testModel1.name = "Thisisatest1"
        testModel1.save()

        testModel1 = TestModel1()
        testModel1.name = "barry"
        testModel1.save()

        testModel1 = TestModel1()
        testModel1.name = "welltest1"
        testModel1.save()

        val called = AtomicBoolean(false)
        val queryTransaction = QueryTransaction.Builder(
                SQLite.select()
                        .from(TestModel1::class.java)
                        .where(TestModel1_Table.name.like("%test1"))
        ).queryResult(QueryTransaction.QueryResultCallback<TestModel1> { transaction, tResult ->
            val results = tResult.toList()
            assertEquals(results.size.toLong(), 2)
            called.set(true)
            tResult.close()
        }).build()

        val transaction = database.beginTransactionAsync(queryTransaction).build()
        MockTransaction(transaction, database).execute()

        assertTrue(called.get())
    }

    @Test
    fun test_bunchaModels() {
        Delete.tables(TestModel2::class.java)

        val modelList = ArrayList<TestModel2>()
        modelList.addAll(GenerationUtils.generateRandomModels(TestModel2::class.java, 10000))

        var startTime = System.currentTimeMillis()

        FlowManager.getDatabase(TestDatabase::class.java)
                .executeTransaction(ProcessModelTransaction.Builder(
                        ProcessModelTransaction.ProcessModel<TestModel2> { model -> model.save() })
                        .addAll(modelList).build())

        println("Transaction completed in: " + (System.currentTimeMillis() - startTime))

        Delete.tables(TestModel2::class.java)
        startTime = System.currentTimeMillis()
        FlowManager.getDatabase(TestDatabase::class.java)
                .executeTransaction(FastStoreModelTransaction.insertBuilder(FlowManager.getModelAdapter(TestModel2::class.java))
                        .addAll(modelList)
                        .build())

        println("Faster Transaction completed in: " + (System.currentTimeMillis() - startTime))

    }

    @Test
    fun test_cachingABunchModels() {
        Delete.tables(TrCacheableModel::class.java)

        val modelAdapter = FlowManager.getModelAdapter(TrCacheableModel::class.java)
        val modelCache = modelAdapter.modelCache

        val modelList = ArrayList<TrCacheableModel>()
        modelList.addAll(GenerationUtils.generateRandomModels(TrCacheableModel::class.java, 10000))

        var startTime = System.currentTimeMillis()

        FlowManager.getDatabase(TestDatabase::class.java)
                .executeTransaction(ProcessModelTransaction.Builder(
                        ProcessModelTransaction.ProcessModel<TrCacheableModel> { model -> model.save() })
                        .addAll(modelList).build())

        println("Transaction completed in: " + (System.currentTimeMillis() - startTime))

        // exists in cache
        for (model in modelList) {
            assertNotNull(modelCache.get(modelAdapter.getCachingId(model)))
        }

        Delete.tables(TrCacheableModel::class.java)
        modelCache.clear()
        startTime = System.currentTimeMillis()
        FlowManager.getDatabase(TestDatabase::class.java)
                .executeTransaction(FastStoreModelTransaction
                        .insertBuilder(modelAdapter)
                        .addAll(modelList)
                        .build())

        // exists in cache
        for (model in modelList) {
            assertNotNull(modelCache.get(modelAdapter.getCachingId(model)))
        }

        println("Faster Transaction completed in: " + (System.currentTimeMillis() - startTime))
    }

}
