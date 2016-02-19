package com.raizlabs.android.dbflow.test.kotlin

import com.raizlabs.android.dbflow.kotlinextensions.database
import com.raizlabs.android.dbflow.kotlinextensions.processInTransaction
import com.raizlabs.android.dbflow.kotlinextensions.transact
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
            .from(TestModel1::class.java).queryList();

        // easily delete all these items.
        items.processInTransaction { it.delete() }

        TestModel1::class.database().transact {
            // do something here
        }
    }

}