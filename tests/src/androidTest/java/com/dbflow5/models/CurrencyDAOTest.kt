package com.dbflow5.models

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.rx2.RXTestRule
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CurrencyDAOTest : BaseUnitTest() {

    lateinit var currencyDAO: CurrencyDAO

    val currency = Currency(symbol = "$", name = "United States Dollar", shortName = "USD")

    @Rule
    @JvmField
    val rxTestRule = RXTestRule()

    @Before
    fun setupTest() {
        currencyDAO = object : CurrencyDAO {
            override val database: TestDatabase = database()
        }
    }

    @Test
    fun validateCoroutine() = runBlockingTest {
        val success = currencyDAO.coroutineStoreUSD(currency).await()
        assert(success) { "Currency didn't save" }
        val result = currencyDAO.coroutineRetrieveUSD().await()
        assert(result.size == 1) { "Results list was empty" }
        assert(result[0] == currency) { "Expected ${currency} but got ${result[0]}" }
    }

    @Test
    fun validateRx() {
        val success = currencyDAO.rxStoreUSD(currency).blockingGet()
        assert(success) { "Currency didn't save" }
        val result = currencyDAO.rxRetrieveUSD().blockingGet()
        assert(result.size == 1) { "Results list was empty" }
        assert(result[0] == currency) { "Expected ${currency} but got ${result[0]}" }
    }


}