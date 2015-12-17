package com.raizlabs.android.dbflow.test.structure.foreignkey;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.test.TestDatabase;
import com.raizlabs.android.dbflow.test.structure.TestModel1;

@Table(database = TestDatabase.class)
public class ForeignModel extends TestModel1 {
    @Column
    @ForeignKey(references =
            {@ForeignKeyReference(columnName = "testmodel_id",
                    foreignKeyColumnName = "name",
                    referencedFieldIsPackagePrivate = true,
                    columnType = String.class)})
    ForeignParentModel testModel1;
}
