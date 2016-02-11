package com.raizlabs.android.dbflow.test.sql;

import android.database.sqlite.SQLiteException;

import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.test.FlowTestCase;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

/**
 * Description: Test to ensure that passing null to non-null fields does not cause a NPE and that it
 * will fail.
 */
public class BoxedValueTest extends FlowTestCase {

    private static final AtomicLong SEQUENCE_ID = new AtomicLong();
    private BoxedModel testObject;

    @Before
    public void setUp() throws Exception {
        testObject = new BoxedModel();
        testObject.id = SEQUENCE_ID.getAndIncrement();
    }

    @Test
    public void testBoxedValues_nullId() {
        testObject.id = null;
        assertCannotSaveModel();
    }

    @Test
    public void testBoxedValues_integerFieldNotNull() {
        testObject.integerFieldNotNull = null;
        assertCannotSaveModel();
    }

    @Test
    public void testBoxedValues_integerField() {
        testObject.integerField = null;
        assertCanSaveModel();
        loadModel();
        assertNull(testObject.integerField);
    }

    @Test
    public void testBoxedValues_stringFieldNotNull() {
        testObject.stringFieldNotNull = null;
        assertCannotSaveModel();
    }

    @Test
    public void testBoxedValues_stringField() {
        testObject.stringField = null;
        assertCanSaveModel();
        loadModel();
        assertNull(testObject.stringField);
    }

    private void loadModel() {
        testObject = new Select()
                .from(BoxedModel.class)
                .where(BoxedModel_Table.id.eq(testObject.id))
                .querySingle();
    }

    private void assertCannotSaveModel() {
        try {
            testObject.save();
            fail("Was able to save model");
        } catch (SQLiteException s) {
            // not null should fail
            assertEquals(s.getMessage(), "Cannot execute for last inserted row ID, base error code: 19");
        }
    }

    private void assertCanSaveModel() {
        try {
            testObject.save();
        } catch (SQLiteException s) {
            s.printStackTrace(System.err);
            fail("Was unable to save model: " + s.getMessage());
        }
    }

}
