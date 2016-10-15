package com.raizlabs.android.dbflow.test.typeconverter;

import android.location.Location;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Where;
import com.raizlabs.android.dbflow.test.FlowTestCase;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Description:
 */
public class TypeConverterTest extends FlowTestCase {

    @Test
    public void testConverters() {

        Delete.table(TestType.class);

        TestType testType = new TestType();
        testType.setName("Name");

        long testTime = System.currentTimeMillis();

        // calendar
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(testTime);
        testType.calendar = calendar;


        Date date = new Date(testTime);
        testType.date = date;

        java.sql.Date date1 = new java.sql.Date(testTime);
        testType.sqlDate = date1;

        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject("{ name: test, happy: true }");
            testType.json = jsonObject;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        Location location = new Location("test");
        location.setLatitude(40.5);
        location.setLongitude(40.5);
        testType.location = location;

        testType.save();

        TestType retrieved = new Select().from(TestType.class)
                .where(TestType_Table.name.is("Name"))
                .querySingle();

        assertNotNull(retrieved);

        assertNotNull(retrieved.calendar);
        assertTrue(retrieved.calendar.equals(calendar));

        assertNotNull(retrieved.date);
        assertTrue(retrieved.date.equals(date));

        assertNotNull(retrieved.sqlDate);
        assertTrue(retrieved.sqlDate.equals(date1));

        assertNotNull(retrieved.json);
        assertTrue(retrieved.json.toString().equals(jsonObject.toString()));

        assertNotNull(retrieved.location);
        assertTrue(retrieved.location.getLongitude() == location.getLongitude());
        assertTrue(retrieved.location.getLatitude() == location.getLatitude());

    }

    /**
     * Nullable database columns need to be allowed to receive null values.
     * <p/>
     * Type converters that autobox to native types need to have their behavior checked
     * when null values are present in the database.
     */
    @Test
    public void testConvertersNullValues() {
        SQLite.delete(TestType.class).execute();

        TestType testType = new TestType();
        testType.setName("Name");
        testType.save();

        TestType retrieved = new Select().from(TestType.class)
                .where(TestType_Table.name.is("Name"))
                .querySingle();

        assertNotNull(retrieved);

        assertNull(retrieved.aBoolean);
        assertNull(retrieved.calendar);
        assertNull(retrieved.date);
        assertNull(retrieved.sqlDate);
        assertNull(retrieved.json);
        assertNull(retrieved.location);
    }

    /**
     * Type converters that autobox to native types need to have their behavior checked
     * when null values are present in the database.
     */
    @Test
    public void testConvertersNullDatabaseConversionValues() {
        SQLite.delete(TestType.class).execute();

        TestType testType = new TestType();
        testType.setName("Name");
        testType.save();

        /*
         * NOTE: We don't want to engage the type converter here since we are attempting to test
         * the read behavior of pre-existing null database values and not the write behavior of
         * the type converter.
         */

        SQLite.update(TestType.class)
                .set(TestType_Table.nativeBoolean.is((Boolean) null))
                .where(TestType_Table.name.eq(testType.getName()))
                .execute();

        TestType retrieved = new Select().from(TestType.class)
                .where(TestType_Table.name.is("Name"))
                .querySingle();
        assertNotNull(retrieved);

        assertFalse(retrieved.nativeBoolean);
    }

    /**
     * Explicit test based on {@link GregorianCalendar} (subclass of {@link Calendar}). <br/>
     * Cause {@link Calendar#getInstance()} return a {@link GregorianCalendar} instance.
     */
    @Test
    public void testConvertersQuerySubClass(){
        final GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTimeInMillis(100000);

        final TestType testType = new TestType();
        testType.setName("Name");
        testType.calendar = gregorianCalendar;
        testType.save();

        final Where<TestType> whereQuery = new Select().from(TestType.class)
            .where(TestType_Table.name.is("Name"), TestType_Table.calendar.eq(gregorianCalendar));

        final String query = whereQuery.getQuery();

        assertNotNull(query);
        assertTrue(query.trim().equals("SELECT * FROM `TestType` WHERE `name`='Name' AND `calendar`=100000"));

        final TestType retrieved = whereQuery.querySingle();

        assertNotNull(retrieved);
        assertNotNull(retrieved.calendar);
        assertTrue(retrieved.calendar.equals(gregorianCalendar));
    }
}

