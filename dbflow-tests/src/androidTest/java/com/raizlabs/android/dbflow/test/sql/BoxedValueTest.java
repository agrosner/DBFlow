package com.raizlabs.android.dbflow.test.sql;

import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteException;

import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.test.FlowTestCase;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Description: Test to ensure that passing null to non-null fields does not cause a NPE and that it
 * will fail.
 */
public class BoxedValueTest extends FlowTestCase {

    private static final AtomicLong SEQUENCE_ID = new AtomicLong();
    private BoxedModel testObject;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        testObject = new BoxedModel();
        testObject.id = SEQUENCE_ID.getAndIncrement();
    }

    public void testBoxedValues_nullId() {
        testObject.id = null;
        assertCannotSaveModel();
    }

    public void testBoxedValues_integerFieldNotNull() {
        testObject.integerFieldNotNull = null;
        assertCannotSaveModel();
    }

    public void testBoxedValues_integerField() {
        testObject.integerField = null;
        assertCanSaveModel();
        loadModel();
        assertNull(testObject.integerField);
    }

    public void testBoxedValues_stringFieldNotNull() {
        testObject.stringFieldNotNull = null;
        assertCannotSaveModel();
    }

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
            assertTrue(s instanceof SQLiteConstraintException);
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
