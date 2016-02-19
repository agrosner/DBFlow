package com.raizlabs.android.dbflow.test.kotlin

import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.select
import com.raizlabs.android.dbflow.test.FlowTestCase
import com.raizlabs.android.dbflow.test.structure.TestModel1
import com.raizlabs.android.dbflow.test.structure.TestModel1_Table
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Test

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
class QueryExtensionsTest : FlowTestCase() {

    @Test
    @Throws(Exception::class)
    fun test_canEquals() {

        var query = select(TestModel1_Table.name) {
            from(TestModel1::class.java) {
                where(TestModel1_Table.name.eq("test"))
                    .and(TestModel1_Table.name.isNot("something"))
            }
        }.query

        assertEquals(query.trim(), "SELECT `name` FROM `TestModel1` WHERE `name`='test' AND `name`!='something'")

    }
}


