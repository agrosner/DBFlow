package com.dbflow5

import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.transaction.BaseTransactionManager
import com.dbflow5.transaction.ITransactionQueue
import com.dbflow5.transaction.Transaction

/**
 * Description: Executes all transactions on same thread for testing.
 */
class ImmediateTransactionManager(databaseDefinition: DBFlowDatabase)
    : BaseTransactionManager(ImmediateTransactionQueue(), databaseDefinition)


class ImmediateTransactionQueue : ITransactionQueue {

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