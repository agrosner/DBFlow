package com.dbflow5.test

import com.dbflow5.database.transaction.TransactionDispatcherFactory
import com.dbflow5.transaction.TransactionDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Runs DB transactions on [Dispatchers.Unconfined] so JDBC work is not tied to
 * a kotlinx-coroutines-test scheduler.
 */
class TestTransactionDispatcherFactory(
    private val testDispatcher: CoroutineDispatcher = Dispatchers.Unconfined,
) : TransactionDispatcherFactory {

    override fun create(): TransactionDispatcher {
        return TransactionDispatcher(testDispatcher)
    }
}
