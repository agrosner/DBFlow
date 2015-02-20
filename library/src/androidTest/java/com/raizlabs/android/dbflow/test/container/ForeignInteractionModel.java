package com.raizlabs.android.dbflow.test.container;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKeyAction;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.container.ForeignKeyContainer;
import com.raizlabs.android.dbflow.test.TestDatabase;
import com.raizlabs.android.dbflow.test.structure.TestModel1;
import com.raizlabs.android.dbflow.test.structure.TestModel1$Table;

import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 */
@Table(databaseName = TestDatabase.NAME)
public class ForeignInteractionModel extends TestModel1 {

    @Column(columnType = Column.FOREIGN_KEY,
            references =
                    {@ForeignKeyReference(columnName = "testmodel_id",
                            foreignColumnName = "name",
                            columnType = String.class)},
            onDelete = ForeignKeyAction.CASCADE,
            onUpdate = ForeignKeyAction.CASCADE,
            saveForeignKeyModel = false)
    ForeignKeyContainer<ParentModel> testModel1;

    public TestModel1 getTestModel1() {
        return testModel1 != null ? testModel1.toModel() : null;
    }

    public void setTestModel1(ParentModel model1) {
        testModel1 = new ForeignKeyContainer<>(ParentModel.class);
        Map<String, Object> map = new HashMap<>();
        map.put(TestModel1$Table.NAME, model1.name);
        testModel1.setData(map);
    }
}
