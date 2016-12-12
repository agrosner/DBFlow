package com.raizlabs.android.dbflow.test.concurrent

import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.sql.language.Delete
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.sql.language.SQLite.selectCountOf
import com.raizlabs.android.dbflow.test.FlowTestCase
import com.raizlabs.android.dbflow.test.TestDatabase
import com.raizlabs.android.dbflow.test.structure.TestModel1
import junit.framework.Assert.assertEquals
import org.junit.Test
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ConcurrentModelTest : FlowTestCase() {

    @Test
    @Throws(InterruptedException::class)
    fun testConcurrentInsert() {
        Delete.table(TestModel1::class.java)

        val executorService = Executors.newFixedThreadPool(3)
        for (i in 0..CONCURRENT_INSERT_COUNT - 1) {
            executorService.execute(InsertRunnable())            // fails
            executorService.execute(PlainSqlInsertRunnable());    // passes
        }

        executorService.shutdown()
        executorService.awaitTermination(CONCURRENT_INSERT_TIMEOUT, TimeUnit.MILLISECONDS)

        val modelCount = selectCountOf().from(TestModel1::class.java).count()
        assertEquals(CONCURRENT_INSERT_COUNT * 2, modelCount)
    }

    private class InsertRunnable constructor() : Runnable {

        override fun run() {
            val uuid = UUID.randomUUID().toString()

            val indexModel = TestModel1()
            indexModel.name = uuid

            FlowManager.getModelAdapter(TestModel1::class.java)
                .insert(indexModel)
        }
    }

    private class PlainSqlInsertRunnable constructor() : Runnable {

        override fun run() {
            val uuid = UUID.randomUUID().toString()

            val insert = SQLite.insert(TestModel1::class.java).orFail()
                .values(uuid)

            FlowManager.getDatabase(TestDatabase.NAME).writableDatabase.execSQL(insert.query)
        }
    }

    companion object {
        private val CONCURRENT_INSERT_COUNT: Long = 10
        private val CONCURRENT_INSERT_TIMEOUT = (60 * 1000).toLong()
    }
}
