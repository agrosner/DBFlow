package com.raizlabs.android.dbflow.test.structure;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.test.TestDatabase;
import com.raizlabs.android.dbflow.test.structure.autoincrement.TestModelAI;

@Table(databaseName = TestDatabase.NAME)
public class ForeignModel2 extends TestModel1 {

    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "testmodel_id",
            columnType = Long.class, foreignColumnName = "id")})
    TestModelAI testModelAI;
}
