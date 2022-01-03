package com.dbflow5.prepackaged

import com.dbflow5.DBFlowInstrumentedTestRule
import com.dbflow5.DemoApp
import com.dbflow5.config.database
import com.dbflow5.database.AndroidSQLiteOpenHelper
import com.dbflow5.query.select
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Description: Asserts our prepackaged DB loads.
 */
class PrepackagedDBTest {

    @JvmField
    @Rule
    var dblflowTestRule = DBFlowInstrumentedTestRule.create {
        database<PrepackagedDB>({
            databaseName("prepackaged")
        }, AndroidSQLiteOpenHelper.createHelperCreator(DemoApp.context))
        database<MigratedPrepackagedDB>({
            databaseName("prepackaged_2")
        }, AndroidSQLiteOpenHelper.createHelperCreator(DemoApp.context))
    }

    @Test
    fun assertWeCanLoadFromDB() = runBlockingTest {
        database<PrepackagedDB> { db ->
            val list = (select from Dog::class).queryList(db)
            assertTrue(list.isNotEmpty())
        }
    }

    @Test
    fun assertWeCanLoadFromDBPostMigrate() = runBlockingTest {
        database<MigratedPrepackagedDB> { db ->
            val list = (select from Dog2::class).queryList(db)
            assertTrue(list.isNotEmpty())

            val newData = (select
                from Dog2::class
                where Dog2_Table.breed.eq("NewBreed")
                and Dog2_Table.newField.eq("New Field Data"))
                .querySingle(db)
            assertTrue(newData != null)
        }
    }
}
