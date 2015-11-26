package com.raizlabs.android.dbflow.test.structure.foreignkey;

import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.test.FlowTestCase;
import com.raizlabs.android.dbflow.test.structure.autoincrement.TestModelAI;

import static com.raizlabs.android.dbflow.test.structure.foreignkey.ForeignModel_Table.name;

public class ForeignKeyTest extends FlowTestCase {

    // region Test Foreign Key

    public void testForeignKey() {
        ForeignParentModel parentModel = new ForeignParentModel();
        parentModel.setName("Test");
        parentModel.save();

        ForeignModel foreignModel = new ForeignModel();
        foreignModel.testModel1 = parentModel;
        foreignModel.setName("Test");
        foreignModel.save();

        ForeignModel retrieved = new Select().from(ForeignModel.class)
                .where(name.is("Test"))
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
        foreignModel2.setName("Test");
        foreignModel2.save();

        ForeignModel2 retrieved = new Select().from(ForeignModel2.class)
                .where(ForeignModel2_Table.name.is("Test"))
                .querySingle();
        assertNotNull(retrieved);
        assertNotNull(retrieved.testModelAI);
        assertEquals(retrieved.testModelAI, foreignModel2.testModelAI);
    }

    // endregion Test Foreign Key
}
