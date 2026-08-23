package com.dbflow5.test.usecases

import com.dbflow5.query.select
import com.dbflow5.test.DatabaseTestRule
import com.dbflow5.test.Dog2
import com.dbflow5.test.MigratedPrepackagedDB_Database
import com.dbflow5.test.PrepackagedDB_Database
import com.dbflow5.test.TestRule
import com.dbflow5.test.TestTransactionDispatcherFactory
import com.dbflow5.test.installPrepackagedFixtures
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Description: Asserts our prepackaged DB loads.
 */
class PrepackagedDBTest : TestRule()  {

    init {
        installPrepackagedFixtures()
    }

    val prepackagedDBRule = DatabaseTestRule(PrepackagedDB_Database) {
        copy(
            name = "prepackaged",
            transactionDispatcherFactory = TestTransactionDispatcherFactory()
        )
    }

    val migratedPrepackagedDBRule = DatabaseTestRule(MigratedPrepackagedDB_Database) {
        copy(
            name = "prepackaged_2",
            transactionDispatcherFactory = TestTransactionDispatcherFactory()
        )
    }

    @Test
    fun assertWeCanLoadFromDB() = prepackagedDBRule.runTest {
        val list = dogAdapter.select().list()
        assertTrue(list.isNotEmpty())
    }

    @Test
    fun assertWeCanLoadFromDBPostMigrate() = migratedPrepackagedDBRule.runTest {
        val list = dog2Adapter.select().list()
        assertTrue(list.isNotEmpty())
        (dog2Adapter.select()
            where Dog2.breed.eq("NewBreed")
            and Dog2.newField.eq("New Field Data"))
            .single()
    }
}
