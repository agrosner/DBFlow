package com.raizlabs.android.dbflow.test.typeconverter

import com.raizlabs.android.dbflow.sql.language.Condition
import com.raizlabs.android.dbflow.test.FlowTestCase

import org.junit.Test

import org.junit.Assert.assertEquals

/**
 * Description:

 * @author Andrew Grosner (fuzz)
 */

class TypeConverterPropertyTest : FlowTestCase() {

    @Test
    fun test_canConvertProperties() {

        val eq = TestType_Table.thisHasCustom.eq(true)
        assertEquals("`thisHasCustom`='1'", eq.query)

        val eq1 = TestType_Table.thisHasCustom.invertProperty().eq("1")
        assertEquals("`thisHasCustom`='1'", eq1.query)
    }
}
