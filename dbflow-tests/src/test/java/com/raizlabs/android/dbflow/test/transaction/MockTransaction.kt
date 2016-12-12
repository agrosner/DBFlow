package com.raizlabs.android.dbflow.test.transaction

import com.raizlabs.android.dbflow.config.DatabaseDefinition
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction

/**
 * Description: Provides a way to mock transactions.
 */
class MockTransaction(private val transaction: Transaction, private val databaseDefinition: DatabaseDefinition) {

    fun execute() {
        transaction.transaction().execute(databaseDefinition.writableDatabase)
    }
}
