package com.raizlabs.android.dbflow.test.container;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.test.FlowTestCase;
import com.raizlabs.android.dbflow.test.structure.TestModel1;

/**
 * Description:
 */
public class ForeignKeyModelTest extends FlowTestCase {

    public void testForeignKeyModel() {

        Delete.tables(ForeignInteractionModel.class,TestModel1.class);

        ForeignInteractionModel foreignInteractionModel = new ForeignInteractionModel();
        TestModel1 testModel1 = new TestModel1();
        testModel1.name = "Test";
        foreignInteractionModel.setTestModel1(testModel1);
        foreignInteractionModel.name = "Test2";
        foreignInteractionModel.save(false);


        assertTrue(foreignInteractionModel.exists());

        assertTrue(foreignInteractionModel.testModel1.exists());

        Delete.tables(TestModel1.class, ForeignInteractionModel.class);

    }
}
