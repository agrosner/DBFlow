/*
package com.raizlabs.android.dbflow.test.structure;

import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.test.FlowTestCase;
import com.raizlabs.android.dbflow.test.structure.autoincrement.TestModelAI;

import static com.raizlabs.android.dbflow.sql.language.Condition.column;

*/
/**
 * Description:
 *//*

public class ForeignKeyTest extends FlowTestCase {

    // region Test Foreign Key

    public void testForeignKey() {
        ForeignParentModel parentModel = new ForeignParentModel();
        parentModel.name = "Test";
        parentModel.save();

        ForeignModel foreignModel = new ForeignModel();
        foreignModel.testModel1 = parentModel;
        foreignModel.name = "Test";
        foreignModel.save();

        ForeignModel retrieved = new Select().from(ForeignModel.class)
                .where(column(ForeignModel$Table.NAME).is("Test"))
                .querySingle();
        assertNotNull(retrieved);
        assertNotNull(retrieved.testModel1);
        assertEquals(retrieved.testModel1, foreignModel.testModel1);
    }

    public void testForeignKey2() {

        TestModelAI testModelAI = new TestModelAI();
        testModelAI.name = "TestAI";
        testModelAI.save();

        ForeignModel2 foreignModel2 = new ForeignModel2();
        foreignModel2.testModelAI = testModelAI;
        foreignModel2.name = "Test";
        foreignModel2.save();

        ForeignModel2 retrieved = new Select().from(ForeignModel2.class)
                .where(column(ForeignModel2$Table.NAME).is("Test"))
                .querySingle();
        assertNotNull(retrieved);
        assertNotNull(retrieved.testModelAI);
        assertEquals(retrieved.testModelAI, foreignModel2.testModelAI);
    }

    // endregion Test Foreign Key
}
*/
