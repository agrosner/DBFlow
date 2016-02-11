package com.raizlabs.android.dbflow.test.structure.autoincrement;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.test.FlowTestCase;
import com.raizlabs.android.dbflow.test.structure.TestModel1;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ModelAutoIncrementTest extends FlowTestCase {

    @Test
    public void testModelAutoIncrement() {
        Delete.table(TestModel1.class);

        TestModelAI testModelAI = new TestModelAI();
        testModelAI.name = "Test";
        testModelAI.insert();

        assertTrue(testModelAI.exists());

        TestModelAI testModelAI2 = new TestModelAI();
        testModelAI2.id = testModelAI.id;
        testModelAI2.name = "Test2";
        testModelAI2.update();

        TestModelAI testModelAI3 = new Select().from(TestModelAI.class)
                .where(TestModelAI_Table.id.is(testModelAI.id))
                .querySingle();
        assertEquals(testModelAI3.name, testModelAI2.name);

        testModelAI.delete();
        assertTrue(!testModelAI.exists());

        Delete.table(TestModel1.class);
    }
}
