package com.raizlabs.android.dbflow.test.structure;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.test.TestDatabase;

@Table(database = TestDatabase.class)
public class ForeignModel extends TestModel1 {
    @Column
    @ForeignKey(references =
            {@ForeignKeyReference(columnName = "testmodel_id",
                    foreignKeyColumnName = "name",
                    columnType = String.class)})
    ForeignParentModel testModel1;
}
