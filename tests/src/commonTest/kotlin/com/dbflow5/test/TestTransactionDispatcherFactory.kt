package com.dbflow5.test

import com.dbflow5.database.transaction.TransactionDispatcherFactory
import com.dbflow5.transaction.TransactionDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher

/**
 * Description: Provides [TestDispatcher] wrapping.
 */
class TestTransactionDispatcherFactory(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TransactionDispatcherFactory {

    override fun create(): TransactionDispatcher {
        return TransactionDispatcher(testDispatcher)
    }
}
