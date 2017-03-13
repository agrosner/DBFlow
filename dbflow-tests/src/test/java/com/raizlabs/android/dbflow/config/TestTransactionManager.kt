package com.raizlabs.android.dbflow.config

import com.raizlabs.android.dbflow.config.DatabaseDefinition
import com.raizlabs.android.dbflow.runtime.BaseTransactionManager
import com.raizlabs.android.dbflow.structure.database.transaction.ITransactionQueue
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction

/**
 * Description: Used for testing integration.
 */
class TestTransactionManager(databaseDefinition: DatabaseDefinition)
    : BaseTransactionManager(TestTransactionManager.CustomQueue(), databaseDefinition) {

    class CustomQueue : ITransactionQueue {

        override fun add(transaction: Transaction) = Unit

        override fun cancel(transaction: Transaction) = Unit

        override fun startIfNotAlive() = Unit

        override fun cancel(name: String) = Unit

        override fun quit() = Unit
    }
}
