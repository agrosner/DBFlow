package com.raizlabs.android.dbflow.transaction

import com.raizlabs.android.dbflow.database.DatabaseWrapper

/**
 * Description: Wraps multiple transactions together.
 */
class TransactionWrapper : ITransaction<Any> {

    private val transactions = arrayListOf<ITransaction<Any>>()

    constructor(vararg transactions: ITransaction<Any>) {
        this.transactions.addAll(transactions)
    }

    constructor(transactions: Collection<ITransaction<Any>>) {
        this.transactions.addAll(transactions)
    }

    override fun execute(databaseWrapper: DatabaseWrapper) {
        transactions.forEach { it.execute(databaseWrapper) }
    }
}
