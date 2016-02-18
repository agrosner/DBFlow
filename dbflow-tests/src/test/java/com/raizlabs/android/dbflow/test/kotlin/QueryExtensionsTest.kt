package com.raizlabs.android.dbflow.test.kotlin

import com.raizlabs.android.dbflow.kotlinextensions.select
import com.raizlabs.android.dbflow.test.structure.TestModel1
import com.raizlabs.android.dbflow.test.structure.TestModel1_Table
import org.junit.Test

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
class QueryExtensionsTest {

    @Test
    @Throws(Exception::class)
    fun test_canEquals() {

        select(TestModel1_Table.name) {
            from(TestModel1::class.java) {
                where(TestModel1_Table.name.equals("test") {

                }
            }
        }.queryList();
    }
}