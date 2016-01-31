package com.raizlabs.android.dbflow.test.structure.externalforeignkey;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.test.FlowTestCase;

import static com.raizlabs.android.dbflow.test.structure.foreignkey.ForeignModel_Table.name;

public class ExternalForeignKeyTest extends FlowTestCase {
    public void testExternalForeignKey () {
        Delete.tables(ForeignParentModel.class, ForeignModel.class);

        ForeignParentModel parentModel = new ForeignParentModel();
        parentModel.setName("Test");
        parentModel.save();

        ForeignModel foreignModel = new ForeignModel();
        foreignModel.setForeignParentModel(parentModel);
        foreignModel.setName("Test");
        foreignModel.save();

        ForeignModel retrieved =
            new Select()
                .from(ForeignModel.class)
                .where (name.is("Test"))
                .querySingle();

        assertNotNull(retrieved);
        assertNotNull(retrieved.foreignParentModel);
        assertNotNull(retrieved.foreignParentModel.toModel());
        assertEquals(retrieved.foreignParentModel.toModel(), foreignModel.foreignParentModel.toModel());
    }
}
