package com.dbflow5.prepackaged

import com.dbflow5.test.TestTransactionDispatcherFactory
import com.dbflow5.query.select
import com.dbflow5.test.DatabaseTestRule
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Description: Asserts our prepackaged DB loads.
 */
class PrepackagedDBTest {

    
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
            where Dog2_Table.breed.eq("NewBreed")
            and Dog2_Table.newField.eq("New Field Data"))
            .single()
    }
}
