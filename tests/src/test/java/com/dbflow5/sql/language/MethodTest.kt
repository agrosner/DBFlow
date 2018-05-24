package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.models.TwoColumnModel_Table.id
import com.dbflow5.models.TwoColumnModel_Table.name
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
        assertEquals("AVG(`name`, `id`)", avg(name, id).query)
        assertEquals("COUNT(`name`, `id`)", count(name, id).query)
        assertEquals("GROUP_CONCAT(`name`, `id`)", groupConcat(name, id).query)
        assertEquals("MAX(`name`, `id`)", max(name, id).query)
        assertEquals("MIN(`name`, `id`)", min(name, id).query)
        assertEquals("SUM(`name`, `id`)", sum(name, id).query)
        assertEquals("TOTAL(`name`, `id`)", total(name, id).query)
        assertEquals("CAST(`name` AS INTEGER)", cast(name).`as`(SQLiteType.INTEGER).query)
        assertEquals("REPLACE(`name`, 'Andrew', 'Grosner')", replace(name, "Andrew", "Grosner").query)
    }

    @Test
    fun test_strftime() {
        assertEquals("strftime('%s', 'now')", strftime("%s", "now").query)
    }

    @Test
    fun test_dateMethod() {
        assertEquals("date('now', 'start of month', '+1 month')",
                date("now", "start of month", "+1 month").query)
    }

    @Test
    fun test_datetimeMethod() {
        assertEquals("datetime(1092941466, 'unix epoch')",
                datetime(1092941466, "unix epoch").query)
    }

    @Test
    fun testIfNull() {
        assertEquals("IFNULL(`name`, `id`)", ifNull(name, id).query)
    }

    @Test
    fun testNulllIf() {
        assertEquals("NULLIF(`name`, `id`)", nullIf(name, id).query)
    }

    @Test
    fun testOpMethods() {
        assertEquals("AVG(`name` + `id`)", avg(name + id).query)
        assertEquals("AVG(`name` + `id`)", (avg(name) + id).query)
        assertEquals("AVG(`name` - `id`)", avg(name - id).query)
        assertEquals("AVG(`name` - `id`)", (avg(name) - id).query)
        assertEquals("AVG(`name` / `id`)", avg(name / id).query)
        assertEquals("AVG(`name` * `id`)", (avg(name) * id).query)
        assertEquals("AVG(`name` % `id`)", avg(name % id).query)
        assertEquals("AVG(`name` % `id`)", (avg(name) % id).query)
    }
}