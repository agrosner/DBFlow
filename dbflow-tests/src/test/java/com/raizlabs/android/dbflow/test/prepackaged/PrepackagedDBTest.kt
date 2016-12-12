package com.raizlabs.android.dbflow.test.prepackaged

import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.test.FlowTestCase

import org.junit.Test

import org.junit.Assert.assertTrue

/**
 * Description:
 */
class PrepackagedDBTest : FlowTestCase() {

    @Test
    fun test_canImportPrepackagedData() {

        val list = SQLite.select()
                .from(Dog::class.java)
                .queryList()

        assertTrue(!list.isEmpty())
    }
}
