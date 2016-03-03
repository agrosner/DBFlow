package com.raizlabs.android.dbflow.test.kotlin

import com.raizlabs.android.dbflow.kotlinextensions.*
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.test.FlowTestCase
import com.raizlabs.android.dbflow.test.structure.TestModel1
import org.junit.Test

/**
 * Description:
 */
class DatabaseExtensionsTest : FlowTestCase() {

    @Test
    @Throws(Exception::class)
    fun test_databaseTransaction() {

        var items = SQLite.select()
                .from<TestModel1>().queryList()

        // easily delete all these items.
        items.processInTransaction { it.delete() }

        database<TestModel1>().transact {
            // do something here
        }

        items.processInTransactionAsync { it.delete() }


    }

}