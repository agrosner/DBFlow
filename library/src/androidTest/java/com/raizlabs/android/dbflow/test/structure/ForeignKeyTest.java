package com.raizlabs.android.dbflow.test.structure;

import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.test.FlowTestCase;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class ForeignKeyTest extends FlowTestCase {

    // region Test Foreign Key

    public void testForeignKey() {
        TestModel1 testModel1 = new TestModel1();
        testModel1.name = "Test";
        testModel1.save(false);

        ForeignModel foreignModel = new ForeignModel();
        foreignModel.testModel1 = testModel1;
        foreignModel.name = "Test";
        foreignModel.save(false);

        ForeignModel retrieved = Select.byId(ForeignModel.class, "Test");
        assertNotNull(retrieved);
        assertNotNull(retrieved.testModel1);
        assertEquals(retrieved.testModel1, foreignModel.testModel1);
    }

    public void testForeignKey2() {

        TestModelAI testModelAI = new TestModelAI();
        testModelAI.name = "TestAI";
        testModelAI.save(false);

        ForeignModel2 foreignModel2 = new ForeignModel2();
        foreignModel2.testModelAI = testModelAI;
        foreignModel2.name = "Test";
        foreignModel2.save(false);

        ForeignModel2 retrieved = Select.byId(ForeignModel2.class, "Test");
        assertNotNull(retrieved);
        assertNotNull(retrieved.testModelAI);
        assertEquals(retrieved.testModelAI, foreignModel2.testModelAI);
    }

    // endregion Test Foreign Key
}
