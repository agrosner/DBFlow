package com.raizlabs.dbflow5

import com.raizlabs.dbflow5.database.DBFlowDatabase
import com.raizlabs.dbflow5.transaction.BaseTransactionManager
import com.raizlabs.dbflow5.transaction.ITransactionQueue
import com.raizlabs.dbflow5.transaction.Transaction

/**
 * Description: Executes all transactions on same thread for testing.
 */
class ImmediateTransactionManager2(databaseDefinition: DBFlowDatabase)
    : BaseTransactionManager(ImmediateTransactionQueue2(), databaseDefinition)


class ImmediateTransactionQueue2 : ITransactionQueue {

    override fun add(transaction: Transaction<out Any?>) {
        transaction.newBuilder()
                .runCallbacksOnSameThread(true)
                .build()
                .executeSync()
    }

    override fun cancel(transaction: Transaction<out Any?>) {

    }

    override fun startIfNotAlive() {
    }

    override fun cancel(name: String) {
    }

    override fun quit() {
    }

}