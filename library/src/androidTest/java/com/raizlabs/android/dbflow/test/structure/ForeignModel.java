package com.raizlabs.android.dbflow.test.structure;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
* Author: andrewgrosner
* Contributors: { }
* Description:
*/
@Table(databaseName = TestDatabase.NAME)
public class ForeignModel extends TestModel1 {
    @Column(columnType = Column.FOREIGN_KEY,
            references =
                    {@ForeignKeyReference(columnName = "testmodel_id",
                            foreignColumnName = "name",
                            columnType = String.class)})
    ForeignParentModel testModel1;
}
