package com.raizlabs.android.dbflow.test.structure;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.test.FlowTestCase;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description:
 */
public class ModelAutoIncrementTest extends FlowTestCase {


    public void testModelAutoIncrement() {
        TestModelAI testModelAI = new TestModelAI();
        testModelAI.name = "Test";
        testModelAI.insert(false);

        assertTrue(testModelAI.exists());

        TestModelAI testModelAI2 = new TestModelAI();
        testModelAI2.id = testModelAI.id;
        testModelAI2.name = "Test2";
        testModelAI2.update(false);

        TestModelAI testModelAI3 = Select.byId(TestModelAI.class, testModelAI.id);
        assertEquals(testModelAI3.name, testModelAI2.name);

        testModelAI.delete(false);
        assertTrue(!testModelAI.exists());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        getContext().deleteDatabase(TestDatabase.NAME);
        FlowManager.destroy();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        FlowManager.init(getContext());
    }
}
