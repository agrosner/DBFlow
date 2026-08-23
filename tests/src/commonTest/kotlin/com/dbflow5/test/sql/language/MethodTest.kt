package com.dbflow5.test.sql.language

import com.dbflow5.test.TwoColumnModel
import com.dbflow5.query.methods.avg
import com.dbflow5.query.methods.cast
import com.dbflow5.query.methods.count
import com.dbflow5.query.methods.date
import com.dbflow5.query.methods.datetime
import com.dbflow5.query.methods.groupConcat
import com.dbflow5.query.methods.ifNull
import com.dbflow5.query.methods.max
import com.dbflow5.query.methods.min
import com.dbflow5.query.methods.nullIf
import com.dbflow5.query.methods.random
import com.dbflow5.query.methods.replace
import com.dbflow5.query.methods.strftime
import com.dbflow5.query.methods.sum
import com.dbflow5.query.methods.total
import kotlin.test.Test
import kotlin.test.assertEquals

class MethodTest {

    @Test
    fun testMainMethods() {
        assertEquals(
            "AVG(`name`, `id`)",
            avg(TwoColumnModel.name, TwoColumnModel.id).query
        )
        assertEquals(
            "COUNT(`name`, `id`)",
            count(TwoColumnModel.name, TwoColumnModel.id).query
        )
        assertEquals(
            "GROUP_CONCAT(`name`, `id`)",
            groupConcat(TwoColumnModel.name, TwoColumnModel.id).query
        )
        assertEquals(
            "MAX(`name`, `id`)",
            max<Any>()(TwoColumnModel.name, TwoColumnModel.id).query
        )
        assertEquals(
            "MIN(`name`, `id`)",
            min<Any>()(TwoColumnModel.name, TwoColumnModel.id).query
        )
        assertEquals(
            "SUM(`name`, `id`)",
            sum(TwoColumnModel.name, TwoColumnModel.id).query
        )
        assertEquals(
            "TOTAL(`name`, `id`)",
            total(TwoColumnModel.name, TwoColumnModel.id).query
        )
        assertEquals(
            "CAST(`name` AS INTEGER)",
            cast(TwoColumnModel.name).asInteger().query
        )
        assertEquals(
            "REPLACE(`name`, 'Andrew', 'Grosner')",
            replace(TwoColumnModel.name, "Andrew", "Grosner").query
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
            ifNull(TwoColumnModel.name, TwoColumnModel.id).query
        )
    }

    @Test
    fun testNulllIf() {
        assertEquals(
            "NULLIF(`name`, `id`)",
            nullIf(TwoColumnModel.name, TwoColumnModel.id).query
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
            avg(TwoColumnModel.name + TwoColumnModel.id).query
        )
        assertEquals(
            "AVG(`name` + `id`)",
            avg(TwoColumnModel.name + TwoColumnModel.id).query
        )
        assertEquals(
            "AVG(`name` - `id`)",
            avg(TwoColumnModel.name - TwoColumnModel.id).query
        )
        assertEquals(
            "AVG(`name` - `id`)",
            avg(TwoColumnModel.name - TwoColumnModel.id).query
        )
        assertEquals(
            "AVG(`name` / `id`)",
            avg(TwoColumnModel.name / TwoColumnModel.id).query
        )
        assertEquals(
            "AVG(`name` * `id`)",
            avg(TwoColumnModel.name * TwoColumnModel.id).query
        )
        assertEquals(
            "AVG(`name` % `id`)",
            avg(TwoColumnModel.name % TwoColumnModel.id).query
        )
        assertEquals(
            "AVG(`name` % `id`)",
            avg(TwoColumnModel.name % TwoColumnModel.id).query
        )
    }
}