package com.dbflow5

import com.dbflow5.config.TransactionDispatcherFactory
import com.dbflow5.transaction.TransactionDispatcher
import kotlinx.coroutines.test.TestCoroutineDispatcher

/**
 * Description: Provides [TestCoroutineDispatcher] wrapping.
 */
class TestTransactionDispatcherFactory(
    private val testDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher(),
) : TransactionDispatcherFactory {

    override fun create(): TransactionDispatcher {
        return TransactionDispatcher(testDispatcher)
    }
}
