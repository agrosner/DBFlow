package com.raizlabs.android.dbflow.typeconverter

import android.location.Location
import com.raizlabs.android.dbflow.sql.language.Delete
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.sql.language.Select
import com.raizlabs.android.dbflow.FlowTestCase
import org.json.JSONException
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.util.*

/**
 * Description:
 */
class TypeConverterTest : FlowTestCase() {

    @Test
    fun testConverters() {

        Delete.table(TestType::class.java)

        val testType = TestType()
        testType.name = "Name"

        val testTime = System.currentTimeMillis()

        // calendar
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = testTime
        testType.calendar = calendar


        val date = Date(testTime)
        testType.date = date

        val date1 = java.sql.Date(testTime)
        testType.sqlDate = date1

        val jsonObject: JSONObject
        try {
            jsonObject = JSONObject("{ name: test, happy: true }")
            testType.json = jsonObject
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }

        val location = Location("test")
        location.latitude = 40.5
        location.longitude = 40.5
        testType.location = location

        testType.save()

        val retrieved = Select().from(TestType::class.java)
            .where(TestType_Table.name.`is`("Name"))
            .querySingle()

        assertNotNull(retrieved)

        assertNotNull(retrieved!!.calendar)
        assertTrue(retrieved.calendar == calendar)

        assertNotNull(retrieved.date)
        assertTrue(retrieved.date == date)

        assertNotNull(retrieved.sqlDate)
        assertTrue(retrieved.sqlDate == date1)

        assertNotNull(retrieved.json)
        assertTrue(retrieved.json!!.toString() == jsonObject.toString())

        assertNotNull(retrieved.location)
        assertTrue(retrieved.location!!.longitude == location.longitude)
        assertTrue(retrieved.location!!.latitude == location.latitude)

    }

    /**
     * Nullable database columns need to be allowed to receive null values.
     *
     *
     * Type converters that autobox to native types need to have their behavior checked
     * when null values are present in the database.
     */
    @Test
    fun testConvertersNullValues() {
        SQLite.delete(TestType::class.java).execute()

        val testType = TestType()
        testType.name = "Name"
        testType.save()

        val retrieved = Select().from(TestType::class.java)
            .where(TestType_Table.name.`is`("Name"))
            .querySingle()

        assertNotNull(retrieved)

        assertNull(retrieved!!.aBoolean)
        assertNull(retrieved.calendar)
        assertNull(retrieved.date)
        assertNull(retrieved.sqlDate)
        assertNull(retrieved.json)
        assertNull(retrieved.location)
    }

    /**
     * Type converters that autobox to native types need to have their behavior checked
     * when null values are present in the database.
     */
    @Test
    fun testConvertersNullDatabaseConversionValues() {
        SQLite.delete(TestType::class.java).execute()

        val testType = TestType()
        testType.name = "Name"
        testType.save()

        /*
         * NOTE: We don't want to engage the type converter here since we are attempting to test
         * the read behavior of pre-existing null database values and not the write behavior of
         * the type converter.
         */

        SQLite.update(TestType::class.java)
            .set(TestType_Table.nativeBoolean.`is`(null as Boolean?))
            .where(TestType_Table.name.eq(testType.name))
            .execute()

        val retrieved = Select().from(TestType::class.java)
            .where(TestType_Table.name.`is`("Name"))
            .querySingle()
        assertNotNull(retrieved)

        assertFalse(retrieved!!.nativeBoolean)
    }

}

