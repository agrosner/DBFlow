package com.raizlabs.android.dbflow.test.structure;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.container.JSONModel;
import com.raizlabs.android.dbflow.structure.container.MapModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

@Table(databaseName = TestDatabase.NAME)
public class ComplexModel extends BaseModel {

    @Column
    @PrimaryKey
    String name;

    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "testmodel_id",
            columnType = String.class, foreignKeyColumnName = "name")})
    JSONModel<TestModel1> testModel1;

    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "mapmodel_id",
            columnType = String.class, foreignKeyColumnName = "name")})
    MapModel<TestModel2> mapModel;

}
