package com.raizlabs.android.dbflow.prepackaged

import com.raizlabs.android.dbflow.BaseInstrumentedUnitTest
import com.raizlabs.android.dbflow.sql.language.from
import com.raizlabs.android.dbflow.sql.language.select
import com.raizlabs.android.dbflow.sql.queriable.list
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Description: Asserts our prepackaged DB loads.
 */
class PrepackagedDBTest : BaseInstrumentedUnitTest() {

    @Test
    fun assertWeCanLoadFromDB() {
        val list = (select from Dog::class).list

        assertTrue(!list.isEmpty())
    }
}
