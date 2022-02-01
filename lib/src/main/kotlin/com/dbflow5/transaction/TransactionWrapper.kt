package com.dbflow5.transaction

import com.dbflow5.database.DatabaseWrapper

/**
 * Description: Wraps multiple transactions together.
 */
class TransactionWrapper : SuspendableTransaction<Any> {

    private val transactions = arrayListOf<SuspendableTransaction<Any>>()

    constructor(vararg transactions: SuspendableTransaction<Any>) {
        this.transactions.addAll(transactions)
    }

    constructor(transactions: Collection<SuspendableTransaction<Any>>) {
        this.transactions.addAll(transactions)
    }

    override suspend fun execute(db: DatabaseWrapper) {
        transactions.forEach { it.execute(db) }
    }
}
