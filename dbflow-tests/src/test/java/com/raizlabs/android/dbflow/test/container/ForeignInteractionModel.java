package com.raizlabs.android.dbflow.test.container;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyAction;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.test.TestDatabase;
import com.raizlabs.android.dbflow.test.structure.TestModel1;

/**
 * Description:
 */
@Table(database = TestDatabase.class)
public class ForeignInteractionModel extends TestModel1 {

    @Column
    @ForeignKey(
<<<<<<< HEAD
        onDelete = ForeignKeyAction.CASCADE,
        onUpdate = ForeignKeyAction.CASCADE,
        references = {
            @ForeignKeyReference(columnName = "testmodel_id", foreignKeyColumnName = "name", columnType = String.class),
            @ForeignKeyReference(columnName = "testmodel_type", foreignKeyColumnName = "type", columnType = String.class)
        },
        saveForeignKeyModel = true)
    ForeignKeyContainer<ParentModel> testModel1;
=======
            onDelete = ForeignKeyAction.CASCADE,
            onUpdate = ForeignKeyAction.CASCADE,
            references =
                    {@ForeignKeyReference(columnName = "testmodel_id",
                            foreignKeyColumnName = "name",
                            referencedFieldIsPackagePrivate = true,
                            columnType = String.class),
                            @ForeignKeyReference(columnName = "testmodel_type",
                                    foreignKeyColumnName = "type",
                                    referencedFieldIsPackagePrivate = true,
                                    columnType = String.class)}
    )
    ParentModel testModel1;
>>>>>>> raizlabs/develop

    public TestModel1 getTestModel1() {
        return testModel1 != null ? testModel1 : null;
    }

    public void setTestModel1(ParentModel model1) {
        testModel1 = model1;
    }
}
