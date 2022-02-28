package com.dbflow5

import com.dbflow5.database.transaction.TransactionDispatcherFactory
import com.dbflow5.transaction.TransactionDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher

/**
 * Description: Provides [TestDispatcher] wrapping.
 */
class TestTransactionDispatcherFactory(
    private val testDispatcher: TestDispatcher = StandardTestDispatcher(),
) : TransactionDispatcherFactory {

    override fun create(): TransactionDispatcher {
        return TransactionDispatcher(testDispatcher)
    }
}
