package com.raizlabs.android.dbflow

import com.raizlabs.android.dbflow.config.DatabaseDefinition
import com.raizlabs.android.dbflow.runtime.BaseTransactionManager
import com.raizlabs.android.dbflow.structure.database.transaction.ITransactionQueue
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction

/**
 * Description: Executes all transactions on same thread for testing.
 */
class ImmediateTransactionManager2(databaseDefinition: DatabaseDefinition)
    : BaseTransactionManager(ImmediateTransactionQueue2(), databaseDefinition)


class ImmediateTransactionQueue2 : ITransactionQueue {

    override fun add(transaction: Transaction) {
        transaction.newBuilder()
                .runCallbacksOnSameThread(true)
                .build()
                .executeSync()
    }

    override fun cancel(transaction: Transaction) {

    }

    override fun startIfNotAlive() {
    }

    override fun cancel(name: String) {
    }

    override fun quit() {
    }

}