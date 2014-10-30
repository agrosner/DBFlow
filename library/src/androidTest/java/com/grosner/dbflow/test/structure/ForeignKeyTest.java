package com.grosner.dbflow.test.structure;

import com.grosner.dbflow.config.DBConfiguration;
import com.grosner.dbflow.test.FlowTestCase;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class ForeignKeyTest extends FlowTestCase {
    @Override
    protected String getDBName() {
        return "foreignkey";
    }

    @Override
    protected void modifyConfiguration(DBConfiguration.Builder builder) {
        builder.setModelClasses(TestModel1.class, ForeignModel.class);
    }

    // region Test Foreign Key

    public void testForeignKey() {
        TestModel1 testModel1 = new TestModel1();
        testModel1.name = "Test";
        testModel1.save(false);

        ForeignModel foreignModel = new ForeignModel();
        foreignModel.testModel1 = testModel1;
        foreignModel.name = "Test";
        foreignModel.save(false);

        // For now will comment it out.
        /*TransactionManager transactionManager = new TransactionManager(mManager, "Foreign Test", false);

        ForeignModel retrieved = transactionManager.selectModelById(ForeignModel.class, "Test");
        assertNotNull(retrieved);
        assertNotNull(retrieved.testModel1);
        assertEquals(retrieved.testModel1, foreignModel.testModel1);*/
    }

    // endregion Test Foreign Key
}
