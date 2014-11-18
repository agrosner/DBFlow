package com.grosner.dbflow.test.structure;

import com.grosner.dbflow.sql.language.Select;
import com.grosner.dbflow.test.FlowTestCase;

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

    // endregion Test Foreign Key
}
