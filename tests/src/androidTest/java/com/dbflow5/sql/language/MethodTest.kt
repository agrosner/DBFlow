package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.models.TwoColumnModel_Table
import com.dbflow5.query.operations.avg
import com.dbflow5.query.operations.cast
import com.dbflow5.query.operations.count
import com.dbflow5.query.operations.date
import com.dbflow5.query.operations.datetime
import com.dbflow5.query.operations.groupConcat
import com.dbflow5.query.operations.ifNull
import com.dbflow5.query.operations.max
import com.dbflow5.query.operations.min
import com.dbflow5.query.operations.nullIf
import com.dbflow5.query.operations.random
import com.dbflow5.query.operations.replace
import com.dbflow5.query.operations.strftime
import com.dbflow5.query.operations.sum
import com.dbflow5.query.operations.total
import org.junit.Assert.assertEquals
import org.junit.Test

class MethodTest : BaseUnitTest() {

    @Test
    fun testMainMethods() {
        assertEquals(
            "AVG(`name`, `id`)",
            avg(TwoColumnModel_Table.name, TwoColumnModel_Table.id).query
        )
        assertEquals(
            "COUNT(`name`, `id`)",
            count(TwoColumnModel_Table.name, TwoColumnModel_Table.id).query
        )
        assertEquals(
            "GROUP_CONCAT(`name`, `id`)",
            groupConcat(TwoColumnModel_Table.name, TwoColumnModel_Table.id).query
        )
        assertEquals(
            "MAX(`name`, `id`)",
            max<Any>()(TwoColumnModel_Table.name, TwoColumnModel_Table.id).query
        )
        assertEquals(
            "MIN(`name`, `id`)",
            min<Any>()(TwoColumnModel_Table.name, TwoColumnModel_Table.id).query
        )
        assertEquals(
            "SUM(`name`, `id`)",
            sum(TwoColumnModel_Table.name, TwoColumnModel_Table.id).query
        )
        assertEquals(
            "TOTAL(`name`, `id`)",
            total(TwoColumnModel_Table.name, TwoColumnModel_Table.id).query
        )
        assertEquals(
            "CAST(`name` AS INTEGER)",
            cast(TwoColumnModel_Table.name).asInteger().query
        )
        assertEquals(
            "REPLACE(`name`, 'Andrew', 'Grosner')",
            replace(TwoColumnModel_Table.name, "Andrew", "Grosner").query
        )
    }

    @Test
    fun test_strftime() {
        assertEquals("strftime('%s', 'now')", strftime("%s", "now").query)
    }

    @Test
    fun test_dateMethod() {
        assertEquals(
            "date('now', 'start of month', '+1 month')",
            date("now", "start of month", "+1 month").query
        )
    }

    @Test
    fun test_datetimeMethod() {
        assertEquals(
            "datetime(1092941466, 'unix epoch')",
            datetime(1092941466, "unix epoch").query
        )
    }

    @Test
    fun testIfNull() {
        assertEquals(
            "IFNULL(`name`, `id`)",
            ifNull(TwoColumnModel_Table.name, TwoColumnModel_Table.id).query
        )
    }

    @Test
    fun testNulllIf() {
        assertEquals(
            "NULLIF(`name`, `id`)",
            nullIf(TwoColumnModel_Table.name, TwoColumnModel_Table.id).query
        )
    }

    @Test
    fun random_generates_correct_query() {
        assertEquals("RANDOM()", random.query)
    }

    @Test
    fun testOpMethods() {
        assertEquals(
            "AVG(`name` + `id`)",
            avg(TwoColumnModel_Table.name + TwoColumnModel_Table.id).query
        )
        assertEquals(
            "AVG(`name` + `id`)",
            avg(TwoColumnModel_Table.name + TwoColumnModel_Table.id).query
        )
        assertEquals(
            "AVG(`name` - `id`)",
            avg(TwoColumnModel_Table.name - TwoColumnModel_Table.id).query
        )
        assertEquals(
            "AVG(`name` - `id`)",
            avg(TwoColumnModel_Table.name - TwoColumnModel_Table.id).query
        )
        assertEquals(
            "AVG(`name` / `id`)",
            avg(TwoColumnModel_Table.name / TwoColumnModel_Table.id).query
        )
        assertEquals(
            "AVG(`name` * `id`)",
            avg(TwoColumnModel_Table.name * TwoColumnModel_Table.id).query
        )
        assertEquals(
            "AVG(`name` % `id`)",
            avg(TwoColumnModel_Table.name % TwoColumnModel_Table.id).query
        )
        assertEquals(
            "AVG(`name` % `id`)",
            avg(TwoColumnModel_Table.name % TwoColumnModel_Table.id).query
        )
    }
}