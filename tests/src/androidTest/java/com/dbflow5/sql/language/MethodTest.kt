package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.models.TwoColumnModel_Table
import com.dbflow5.query.avg
import com.dbflow5.query.cast
import com.dbflow5.query.count
import com.dbflow5.query.date
import com.dbflow5.query.datetime
import com.dbflow5.query.groupConcat
import com.dbflow5.query.ifNull
import com.dbflow5.query.max
import com.dbflow5.query.min
import com.dbflow5.query.nullIf
import com.dbflow5.query.random
import com.dbflow5.query.replace
import com.dbflow5.query.strftime
import com.dbflow5.query.sum
import com.dbflow5.query.total
import com.dbflow5.sql.SQLiteType
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
            max(TwoColumnModel_Table.name, TwoColumnModel_Table.id).query
        )
        assertEquals(
            "MIN(`name`, `id`)",
            min(TwoColumnModel_Table.name, TwoColumnModel_Table.id).query
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
            cast(TwoColumnModel_Table.name).`as`(SQLiteType.INTEGER).query
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
            (avg(TwoColumnModel_Table.name) + TwoColumnModel_Table.id).query
        )
        assertEquals(
            "AVG(`name` - `id`)",
            avg(TwoColumnModel_Table.name - TwoColumnModel_Table.id).query
        )
        assertEquals(
            "AVG(`name` - `id`)",
            (avg(TwoColumnModel_Table.name) - TwoColumnModel_Table.id).query
        )
        assertEquals(
            "AVG(`name` / `id`)",
            avg(TwoColumnModel_Table.name / TwoColumnModel_Table.id).query
        )
        assertEquals(
            "AVG(`name` * `id`)",
            (avg(TwoColumnModel_Table.name) * TwoColumnModel_Table.id).query
        )
        assertEquals(
            "AVG(`name` % `id`)",
            avg(TwoColumnModel_Table.name % TwoColumnModel_Table.id).query
        )
        assertEquals(
            "AVG(`name` % `id`)",
            (avg(TwoColumnModel_Table.name) % TwoColumnModel_Table.id).query
        )
    }
}