package com.raizlabs.android.dbflow.test.kotlin

import com.raizlabs.android.dbflow.kotlinextensions.database
import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.processInTransaction
import com.raizlabs.android.dbflow.kotlinextensions.processInTransactionAsync
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction
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
        items.processInTransaction { it, databaseWrapper -> it.delete(databaseWrapper) }

        database<TestModel1>().executeTransaction {
            // do something here
        }

        items.processInTransactionAsync { it, databaseWrapper -> it.save(databaseWrapper) }

        items.processInTransactionAsync({ it, databaseWrapper -> it.delete(databaseWrapper) },
                Transaction.Success {
                    // do something here
                },
                Transaction.Error { transaction, throwable ->

                })
        items.processInTransactionAsync({ it, databaseWrapper -> it.delete(databaseWrapper) },
                ProcessModelTransaction.OnModelProcessListener { current, size, model ->
                    // do something here
                })
    }

}