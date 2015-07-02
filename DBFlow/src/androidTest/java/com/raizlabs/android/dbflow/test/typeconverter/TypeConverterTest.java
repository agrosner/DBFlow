package com.raizlabs.android.dbflow.test.typeconverter;

import android.location.Location;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Update;
import com.raizlabs.android.dbflow.test.FlowTestCase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

import static com.raizlabs.android.dbflow.sql.builder.Condition.column;

/**
 * Description:
 */
public class TypeConverterTest extends FlowTestCase {

    public void testConverters() {

        Delete.table(TestType.class);

        TestType testType = new TestType();
        testType.name = "Name";

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
                .where(column(TestType$Table.NAME).is("Name"))
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
     */
    public void testConvertersNullValues() {
        new Delete().from(TestType.class).where().query();

        TestType testType = new TestType();
        testType.name = "Name";
        testType.save();

        TestType retrieved = new Select().from(TestType.class)
                .where(column(TestType$Table.NAME).is("Name"))
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
    public void testConvertersNullDatabaseConversionValues() {
        new Delete().from(TestType.class).where().query();

        TestType testType = new TestType();
        testType.name = "Name";
        testType.save();

        /*
         * NOTE: We don't want to engage the type converter here since we are attempting to test
         * the read behavior of pre-existing null database values and not the write behavior of
         * the type converter.
         */
        new Update<>(TestType.class)
                .set(TestType$Table.NATIVEBOOLEAN + " = null")
                .where(column(TestType$Table.NAME).eq(testType.name))
                .queryClose();

        TestType retrieved = new Select().from(TestType.class)
                .where(column(TestType$Table.NAME).is("Name"))
                .querySingle();
        assertNotNull(retrieved);

        assertFalse(retrieved.nativeBoolean);
    }

}
