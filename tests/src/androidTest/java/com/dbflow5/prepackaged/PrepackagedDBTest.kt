package com.dbflow5.prepackaged

import com.dbflow5.query.select
import com.dbflow5.test.DatabaseTestRule
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Description: Asserts our prepackaged DB loads.
 */
class PrepackagedDBTest {

    @get:Rule
    val prepackagedDBRule = DatabaseTestRule {
        PrepackagedDB_Database.create {
            copy(name = "prepackaged")
        }
    }

    @get:Rule
    val migratedPrepackagedDBRule = DatabaseTestRule {
        MigratedPrepackagedDB_Database.create {
            copy(name = "prepackaged_2")
        }
    }

    @Test
    fun assertWeCanLoadFromDB() = prepackagedDBRule.runBlockingTest {
        val list = dogAdapter.select().list()
        assertTrue(list.isNotEmpty())
    }

    @Test
    fun assertWeCanLoadFromDBPostMigrate() = migratedPrepackagedDBRule.runBlockingTest {
        val list = dog2Adapter.select().list()
        assertTrue(list.isNotEmpty())
        (dog2Adapter.select()
            where Dog2_Table.breed.eq("NewBreed")
            and Dog2_Table.newField.eq("New Field Data"))
            .single()
    }
}
