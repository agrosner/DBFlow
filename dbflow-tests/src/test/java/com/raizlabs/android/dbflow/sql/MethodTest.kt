package com.raizlabs.android.dbflow.sql

import com.raizlabs.android.dbflow.sql.SQLiteType
import com.raizlabs.android.dbflow.sql.language.Method
import com.raizlabs.android.dbflow.sql.language.Method.*
import com.raizlabs.android.dbflow.FlowTestCase
import com.raizlabs.android.dbflow.structure.TestModel1_Table.name
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Description: Tests a [Method] class.
 */
class MethodTest : FlowTestCase() {

    @Test
    fun test_avgMethod() {
        val query = avg(name).query
        assertEquals(query, "AVG(`name`)")
    }

    @Test
    fun test_countMethod() {
        val query = count(name).query
        assertEquals(query, "COUNT(`name`)")
    }

    @Test
    fun test_groupConcatMethod() {
        val query = group_concat(name).query
        assertEquals(query, "GROUP_CONCAT(`name`)")
    }

    @Test
    fun test_maxMethod() {
        val query = max(name).query
        assertEquals(query, "MAX(`name`)")
    }

    @Test
    fun test_minMethod() {
        val query = min(name).query
        assertEquals(query, "MIN(`name`)")
    }

    @Test
    fun test_sumMethod() {
        val query = sum(name).query
        assertEquals(query, "SUM(`name`)")
    }

    @Test
    fun test_totalMethod() {
        val query = total(name).query
        assertEquals(query, "TOTAL(`name`)")
    }

    @Test
    fun test_castMethod() {
        val query = cast(name).`as`(SQLiteType.INTEGER).query
        assertEquals("CAST(`name` AS INTEGER)", query)
    }

    @Test
    fun test_replaceMethod() {
        val query = replace(name, "ro", "or").query
        assertEquals("REPLACE(`name` , 'ro' , 'or')", query)
    }

    @Test
    fun test_strftime() {
        assertEquals("strftime('%s' , 'now')",
                strftime("%s", "now").query)
    }

    @Test
    fun test_dateMethod() {
        assertEquals("date('now' , 'start of month' , '+1 month')",
                date("now", "start of month", "+1 month").query)
    }

    @Test
    fun test_datetimeMethod() {
        assertEquals("datetime(1092941466 , 'unix epoch')",
                datetime(1092941466, "unix epoch").query)
    }
}
