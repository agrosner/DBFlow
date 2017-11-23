package com.raizlabs.android.dbflow.structure.database.transaction

import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper
import java.util.*

/**
 * Description: Wraps multiple transactions together.
 */
class TransactionWrapper : ITransaction {

    private val transactions = ArrayList<ITransaction>()

    constructor(vararg transactions: ITransaction) {
        this.transactions.addAll(Arrays.asList(*transactions))
    }

    constructor(transactions: Collection<ITransaction>) {
        this.transactions.addAll(transactions)
    }

    override fun execute(databaseWrapper: DatabaseWrapper) {
        transactions.forEach { it.execute(databaseWrapper) }
    }
}
