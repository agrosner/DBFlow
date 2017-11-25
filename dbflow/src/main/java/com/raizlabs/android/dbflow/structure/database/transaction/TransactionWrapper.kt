package com.raizlabs.android.dbflow.structure.database.transaction

import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper
import java.util.*

/**
 * Description: Wraps multiple transactions together.
 */
class TransactionWrapper : ITransaction<Any> {

    private val transactions = ArrayList<ITransaction<Any>>()

    constructor(vararg transactions: ITransaction<Any>) {
        this.transactions.addAll(Arrays.asList(*transactions))
    }

    constructor(transactions: Collection<ITransaction<Any>>) {
        this.transactions.addAll(transactions)
    }

    override fun execute(databaseWrapper: DatabaseWrapper) {
        transactions.forEach { it.execute(databaseWrapper) }
    }
}
