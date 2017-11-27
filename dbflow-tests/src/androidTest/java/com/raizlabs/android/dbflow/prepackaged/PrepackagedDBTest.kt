package com.raizlabs.android.dbflow.prepackaged

import com.raizlabs.android.dbflow.BaseInstrumentedUnitTest
import com.raizlabs.android.dbflow.config.databaseForTable
import com.raizlabs.android.dbflow.query.select
import com.raizlabs.android.dbflow.query.list
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
