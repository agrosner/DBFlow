package com.raizlabs.android.dbflow.test.structure.autoincrement;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.test.FlowTestCase;
import com.raizlabs.android.dbflow.test.TestDatabase;
import com.raizlabs.android.dbflow.test.structure.TestModel1;

public class ModelAutoIncrementTest extends FlowTestCase {

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
