package com.raizlabs.android.dbflow.test.sql

import com.raizlabs.android.dbflow.sql.language.CursorResult
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.structure.AsyncModel
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction
import com.raizlabs.android.dbflow.test.FlowTestCase
import com.raizlabs.android.dbflow.test.structure.TestModel1
import com.raizlabs.android.dbflow.test.structure.TestModel1_Table

import org.junit.Test

import org.junit.Assert.assertFalse

/**
 * Description:
 */
class AsyncQueryTest : FlowTestCase() {

    @Test
    fun testAsyncQuery() {
        val testModel1 = TestModel1()
        testModel1.name = "Async"
        testModel1.save()

        SQLite.select().from(TestModel1::class.java)
                .where(TestModel1_Table.name.`is`("Async"))
                .async()
                .queryResultCallback { transaction, tResult -> }.execute()

        SQLite.update(TestModel1::class.java)
                .set(TestModel1_Table.name.`is`("Async2"))
                .where(TestModel1_Table.name.`is`("Async"))
                .async().execute()

        testModel1.async().withListener { model -> assertFalse(model.exists()) }.delete()

    }
}
