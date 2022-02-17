package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.models.TwoColumnModel_Table
import com.dbflow5.query.operations.StandardMethods
import com.dbflow5.query.operations.StandardMethods.*
import com.dbflow5.query.operations.invoke
import com.dbflow5.sql.SQLiteType
import org.junit.Assert.assertEquals
import org.junit.Test

class MethodTest : BaseUnitTest() {

    @Test
    fun testMainMethods() {
        assertEquals(
            "AVG(`name`, `id`)",
            Avg(TwoColumnModel_Table.name, TwoColumnModel_Table.id).query
        )
        assertEquals(
            "COUNT(`name`, `id`)",
            Count(TwoColumnModel_Table.name, TwoColumnModel_Table.id).query
        )
        assertEquals(
            "GROUP_CONCAT(`name`, `id`)",
            GroupConcat(TwoColumnModel_Table.name, TwoColumnModel_Table.id).query
        )
        assertEquals(
            "MAX(`name`, `id`)",
            Max<Any>()(TwoColumnModel_Table.name, TwoColumnModel_Table.id).query
        )
        assertEquals(
            "MIN(`name`, `id`)",
            Min<Any>()(TwoColumnModel_Table.name, TwoColumnModel_Table.id).query
        )
        assertEquals(
            "SUM(`name`, `id`)",
            Sum(TwoColumnModel_Table.name, TwoColumnModel_Table.id).query
        )
        assertEquals(
            "TOTAL(`name`, `id`)",
            Total(TwoColumnModel_Table.name, TwoColumnModel_Table.id).query
        )
        assertEquals(
            "CAST(`name` AS INTEGER)",
            Cast(TwoColumnModel_Table.name).asInteger().query
        )
        assertEquals(
            "REPLACE(`name`, 'Andrew', 'Grosner')",
            Replace(TwoColumnModel_Table.name, "Andrew", "Grosner").query
        )
    }

    @Test
    fun test_strftime() {
        assertEquals("strftime('%s', 'now')", StrfTime("%s", "now").query)
    }

    @Test
    fun test_dateMethod() {
        assertEquals(
            "date('now', 'start of month', '+1 month')",
            Date("now", "start of month", "+1 month").query
        )
    }

    @Test
    fun test_datetimeMethod() {
        assertEquals(
            "datetime(1092941466, 'unix epoch')",
            DateTime(1092941466, "unix epoch").query
        )
    }

    @Test
    fun testIfNull() {
        assertEquals(
            "IFNULL(`name`, `id`)",
            IfNull(TwoColumnModel_Table.name, TwoColumnModel_Table.id).query
        )
    }

    @Test
    fun testNulllIf() {
        assertEquals(
            "NULLIF(`name`, `id`)",
            NullIf(TwoColumnModel_Table.name, TwoColumnModel_Table.id).query
        )
    }

    @Test
    fun random_generates_correct_query() {
        assertEquals("RANDOM()", Random().query)
    }

    @Test
    fun testOpMethods() {
        assertEquals(
            "AVG(`name` + `id`)",
            Avg(TwoColumnModel_Table.name + TwoColumnModel_Table.id).query
        )
        assertEquals(
            "AVG(`name` + `id`)",
            Avg(TwoColumnModel_Table.name + TwoColumnModel_Table.id).query
        )
        assertEquals(
            "AVG(`name` - `id`)",
            Avg(TwoColumnModel_Table.name - TwoColumnModel_Table.id).query
        )
        assertEquals(
            "AVG(`name` - `id`)",
            Avg(TwoColumnModel_Table.name - TwoColumnModel_Table.id).query
        )
        assertEquals(
            "AVG(`name` / `id`)",
            Avg(TwoColumnModel_Table.name / TwoColumnModel_Table.id).query
        )
        assertEquals(
            "AVG(`name` * `id`)",
            Avg(TwoColumnModel_Table.name * TwoColumnModel_Table.id).query
        )
        assertEquals(
            "AVG(`name` % `id`)",
            Avg(TwoColumnModel_Table.name % TwoColumnModel_Table.id).query
        )
        assertEquals(
            "AVG(`name` % `id`)",
            Avg(TwoColumnModel_Table.name % TwoColumnModel_Table.id).query
        )
    }
}