package com.dbflow5.prepackaged

import com.dbflow5.BaseInstrumentedUnitTest
import com.dbflow5.config.databaseForTable
import com.dbflow5.query.list
import com.dbflow5.query.select
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Description: Asserts our prepackaged DB loads.
 */
class PrepackagedDBTest : BaseInstrumentedUnitTest() {

    @Test
    fun assertWeCanLoadFromDB() {
        databaseForTable<Dog> {
            val list = (select from Dog::class).list
            assertTrue(!list.isEmpty())
        }
    }
}
