package com.dbflow5.prepackaged

import com.dbflow5.BaseUnitTest
import com.dbflow5.config.databaseForTable
import com.dbflow5.query.select
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Description: Asserts our prepackaged DB loads.
 */
class PrepackagedDBTest : BaseUnitTest() {

    @Test
    fun assertWeCanLoadFromDB() {
        databaseForTable<Dog> { dbFlowDatabase ->
            val list = (select from Dog::class).queryList(dbFlowDatabase)
            assertTrue(!list.isEmpty())
        }
    }
}
