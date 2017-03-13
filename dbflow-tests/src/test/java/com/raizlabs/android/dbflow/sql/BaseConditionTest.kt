package com.raizlabs.android.dbflow.sql

import com.raizlabs.android.dbflow.data.Blob
import com.raizlabs.android.dbflow.sql.Query
import com.raizlabs.android.dbflow.sql.language.BaseCondition
import com.raizlabs.android.dbflow.sql.language.BaseModelQueriable
import com.raizlabs.android.dbflow.sql.language.Condition
import com.raizlabs.android.dbflow.sql.language.NameAlias
import com.raizlabs.android.dbflow.sql.language.SQLCondition
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.FlowTestCase
import com.raizlabs.android.dbflow.structure.TestModel1

import org.junit.Test

import java.util.Date

import com.raizlabs.android.dbflow.sql.language.Condition.column
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue

/**
 * Description: Tests a few methods of [BaseCondition]
 */
class BaseConditionTest : FlowTestCase() {

    @Test
    fun test_canGetNull() {
        val `val`: Any? = null
        val str = BaseCondition.convertValueToString(`val`, false)
        assertEquals("NULL", str)
    }

    @Test
    fun test_canConvertNumber() {
        val num = 5
        val str = BaseCondition.convertValueToString(num, false)
        assertEquals("5", str)
    }

    @Test
    fun test_typeConvertValue() {
        val time = System.currentTimeMillis()
        val date = Date(time)
        val str = BaseCondition.convertValueToString(date, false)
        assertEquals("" + time, str)
    }

    @Test
    fun test_baseModelQueriable() {
        val queriable = SQLite.select()
                .from(TestModel1::class.java)
        val str = BaseCondition.convertValueToString(queriable, true)
        assertEquals("(SELECT * FROM `TestModel1`)", str)
    }

    @Test
    fun test_NameAlias() {
        val nameAlias = NameAlias.builder("Dog")
                .`as`("Cat").build()
        val str = BaseCondition.convertValueToString(nameAlias, false)
        assertEquals(str, "`Cat`")
    }

    @Test
    fun test_sqlCondition() {
        val condition = column(
                NameAlias.Builder("Dog")
                        .build())
                .eq("Cat")
        val str = BaseCondition.convertValueToString(condition, false)
        assertEquals(str, "`Dog`='Cat'")
    }

    @Test
    fun test_query() {
        val query = Query { "Query" }
        val str = BaseCondition.convertValueToString(query, false)
        assertEquals("Query", str)
    }

    @Test
    fun test_Blob() {
        val testBytes = "Bytes".toByteArray()
        val blob = Blob(testBytes)
        var str = BaseCondition.convertValueToString(blob, false)
        // both Blob and byte[] should produce same output.
        assertEquals(str, BaseCondition.convertValueToString(testBytes, false))
        assertTrue(str.startsWith("X"))
        str = String(hexStringToByteArray(str.replace("X'", "").replace("'", "")))
        assertEquals("Bytes", str)
    }

    @Test
    fun test_string() {
        val string = "string"
        val str = BaseCondition.convertValueToString(string, false)
        assertEquals("'string'", str)
    }

    @Test
    fun test_canPassEmptyParam() {
        val empty = Condition.Operation.EMPTY_PARAM
        val str = BaseCondition.convertValueToString(empty, false)
        assertEquals(Condition.Operation.EMPTY_PARAM, str)
    }

    private fun hexStringToByteArray(s: String): ByteArray {
        val len = s.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }
}
