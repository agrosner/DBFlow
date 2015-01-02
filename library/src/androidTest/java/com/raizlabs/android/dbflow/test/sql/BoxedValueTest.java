package com.raizlabs.android.dbflow.test.sql;

import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteException;

import com.raizlabs.android.dbflow.test.FlowTestCase;

/**
 * Description: Test to ensure that passing null to non-null fields does not cause a NPE and that it
 * will fail.
 */
public class BoxedValueTest extends FlowTestCase {

    public void testBoxedValues() {
        BoxedModel boxedModel = new BoxedModel();
        boxedModel.id = null;
        boxedModel.rblNumber = null;

        try {
            boxedModel.save(false);
        } catch (SQLiteException s) {
            // not null should fail
            assertTrue(s instanceof SQLiteConstraintException);
        }
    }
}
