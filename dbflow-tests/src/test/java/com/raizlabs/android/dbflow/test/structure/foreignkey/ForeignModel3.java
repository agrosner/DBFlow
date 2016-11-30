package com.raizlabs.android.dbflow.test.structure.foreignkey;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.test.TestDatabase;
import com.raizlabs.android.dbflow.test.structure.TestModel1;
import com.raizlabs.android.dbflow.test.structure.autoincrement.TestModelAI;

@Table(database = TestDatabase.class)
public class ForeignModel3 extends TestModel1 {

    @Column
    @ForeignKey(
            tableClass = TestModelAI.class,
            references
                    = {@ForeignKeyReference(columnName = "testmodel_id",
                    columnType = Long.class, foreignKeyColumnName = "id"
            )})
    Long testModelAI;
}
