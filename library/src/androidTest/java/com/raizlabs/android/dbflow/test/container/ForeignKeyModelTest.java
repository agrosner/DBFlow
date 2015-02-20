package com.raizlabs.android.dbflow.test.container;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.test.FlowTestCase;
import com.raizlabs.android.dbflow.test.structure.TestModel1;

/**
 * Description:
 */
public class ForeignKeyModelTest extends FlowTestCase {

    public void testForeignKeyModel() {

        Delete.tables(ForeignInteractionModel.class, ParentModel.class);

        ForeignInteractionModel foreignInteractionModel = new ForeignInteractionModel();
        ParentModel parentModel = new ParentModel();
        parentModel.name = "Test";
        parentModel.save(false);
        foreignInteractionModel.setTestModel1(parentModel);
        foreignInteractionModel.name = "Test2";
        foreignInteractionModel.save(false);


        assertTrue(foreignInteractionModel.exists());

        assertTrue(foreignInteractionModel.testModel1.exists());


        foreignInteractionModel = Select.byId(ForeignInteractionModel.class, "Test2");
        assertNotNull(foreignInteractionModel);
        assertNotNull(foreignInteractionModel.testModel1);
        TestModel1 testModel11 = foreignInteractionModel.getTestModel1();
        assertNotNull(parentModel);
        assertEquals("Test", testModel11.name);

        Delete.tables(ForeignInteractionModel.class, ParentModel.class);
    }
}
