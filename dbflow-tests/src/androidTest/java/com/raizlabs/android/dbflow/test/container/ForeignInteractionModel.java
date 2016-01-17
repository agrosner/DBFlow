package com.raizlabs.android.dbflow.test.container;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyAction;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.container.ForeignKeyContainer;
import com.raizlabs.android.dbflow.test.TestDatabase;
import com.raizlabs.android.dbflow.test.structure.TestModel1;

/**
 * Description:
 */
@ModelContainer
@Table(database = TestDatabase.class)
public class ForeignInteractionModel extends TestModel1 {

    @Column
    @ForeignKey(
            onDelete = ForeignKeyAction.CASCADE,
            onUpdate = ForeignKeyAction.CASCADE,
            references =
                    {@ForeignKeyReference(columnName = "testmodel_id",
                            foreignKeyColumnName = "name",
                            columnType = String.class),
                            @ForeignKeyReference(columnName = "testmodel_type",
                                    foreignKeyColumnName = "type",
                                    columnType = String.class)},
            saveForeignKeyModel = false)
    ForeignKeyContainer<ParentModel> testModel1;

    public TestModel1 getTestModel1() {
        return testModel1 != null ? testModel1.toModel() : null;
    }

    public void setTestModel1(ParentModel model1) {
        testModel1 = FlowManager.getContainerAdapter(ParentModel.class).toForeignKeyContainer(model1);
    }
}
