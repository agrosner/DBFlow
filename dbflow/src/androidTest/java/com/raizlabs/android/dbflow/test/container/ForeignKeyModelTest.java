package com.raizlabs.android.dbflow.test.container;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.test.FlowTestCase;
import com.raizlabs.android.dbflow.test.structure.TestModel1;

public class ForeignKeyModelTest extends FlowTestCase {

    public void testForeignKeyModel() {

        Delete.tables(ForeignInteractionModel.class, ParentModel.class);

        ForeignInteractionModel foreignInteractionModel = new ForeignInteractionModel();
        ParentModel parentModel = new ParentModel();
        parentModel.setName("Test");
        parentModel.type = "Type";
        parentModel.save();
        assertTrue(parentModel.exists());

        foreignInteractionModel.setTestModel1(parentModel);
        foreignInteractionModel.setName("Test2");
        foreignInteractionModel.save();


        assertTrue(foreignInteractionModel.exists());

        assertTrue(foreignInteractionModel.testModel1.exists());


        foreignInteractionModel = new Select().from(ForeignInteractionModel.class)
                .where(ForeignInteractionModel_Table.name.is("Test2")).querySingle();
        assertNotNull(foreignInteractionModel);
        assertNotNull(foreignInteractionModel.testModel1);
        TestModel1 testModel11 = foreignInteractionModel.getTestModel1();
        assertNotNull(parentModel);
        assertEquals("Test", testModel11.getName());

        Delete.tables(ForeignInteractionModel.class, ParentModel.class);
    }
}
