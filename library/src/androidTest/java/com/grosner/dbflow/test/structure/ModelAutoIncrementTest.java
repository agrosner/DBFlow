package com.grosner.dbflow.test.structure;

import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.test.FlowTestCase;
import com.grosner.dbflow.test.TestDatabase;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class ModelAutoIncrementTest extends FlowTestCase {


    public void testModelAutoIncrement() {
        TestModelAI testModelAI = new TestModelAI();
        testModelAI.name = "Test";
        testModelAI.save(false);

        assertTrue(testModelAI.exists());

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
