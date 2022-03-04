package com.dbflow5.models

import com.dbflow5.TestDatabase
import com.dbflow5.TestDatabase_Database
import com.dbflow5.rx2.RXTestRule
import com.dbflow5.test.DatabaseTestRule
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import kotlin.test.Test

class CurrencyDAOTest {

    lateinit var currencyDAO: CurrencyDAO

    val currency = Currency(symbol = "$", name = "United States Dollar", shortName = "USD")

    @Rule
    @JvmField
    val rxTestRule = RXTestRule()

    
    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Before
    fun setupTest() {
        currencyDAO = object : CurrencyDAO {
            override val database: TestDatabase = dbRule.db
        }
    }

    @Test
    fun validateCoroutine() = runTest {
        currencyDAO.coroutineStoreUSD(currency).await()
        val result = currencyDAO.coroutineRetrieveUSD().await()
        assert(result.size == 1) { "Results list was empty" }
        assert(result[0] == currency) { "Expected ${currency} but got ${result[0]}" }
    }

    @Test
    fun validateRx() {
        currencyDAO.rxStoreUSD(currency).blockingGet()
        val result = currencyDAO.rxRetrieveUSD().blockingGet()
        assert(result.size == 1) { "Results list was empty" }
        assert(result[0] == currency) { "Expected ${currency} but got ${result[0]}" }
    }


}