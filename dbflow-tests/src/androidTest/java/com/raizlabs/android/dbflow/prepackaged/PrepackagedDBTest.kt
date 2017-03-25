package com.raizlabs.android.dbflow.prepackaged

import com.raizlabs.android.dbflow.BaseInstrumentedUnitTest
import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.list
import com.raizlabs.android.dbflow.kotlinextensions.select
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
