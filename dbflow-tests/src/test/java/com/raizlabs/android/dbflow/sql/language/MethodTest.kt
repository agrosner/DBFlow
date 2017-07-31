package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.models.TwoColumnModel_Table.id
import com.raizlabs.android.dbflow.models.TwoColumnModel_Table.name
import com.raizlabs.android.dbflow.sql.SQLiteType
import com.raizlabs.android.dbflow.sql.language.Method.avg
import com.raizlabs.android.dbflow.sql.language.Method.cast
import com.raizlabs.android.dbflow.sql.language.Method.count
import com.raizlabs.android.dbflow.sql.language.Method.date
import com.raizlabs.android.dbflow.sql.language.Method.datetime
import com.raizlabs.android.dbflow.sql.language.Method.group_concat
import com.raizlabs.android.dbflow.sql.language.Method.ifNull
import com.raizlabs.android.dbflow.sql.language.Method.max
import com.raizlabs.android.dbflow.sql.language.Method.min
import com.raizlabs.android.dbflow.sql.language.Method.nullIf
import com.raizlabs.android.dbflow.sql.language.Method.replace
import com.raizlabs.android.dbflow.sql.language.Method.strftime
import com.raizlabs.android.dbflow.sql.language.Method.sum
import com.raizlabs.android.dbflow.sql.language.Method.total
import org.junit.Assert.assertEquals
import org.junit.Test

class MethodTest : BaseUnitTest() {

    @Test
    fun testMainMethods() {
        assertEquals("AVG(`name`, `id`)", avg(name, id).query)
        assertEquals("COUNT(`name`, `id`)", count(name, id).query)
        assertEquals("GROUP_CONCAT(`name`, `id`)", group_concat(name, id).query)
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