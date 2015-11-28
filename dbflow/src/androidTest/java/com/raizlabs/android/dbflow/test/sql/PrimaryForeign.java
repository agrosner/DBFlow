package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.test.TestDatabase;
import com.raizlabs.android.dbflow.test.structure.TestModel2;

/**
 * Description:
 */
@Table(database = TestDatabase.class)
public class PrimaryForeign extends BaseModel {

    @PrimaryKey
    @ForeignKey(tableClass = TestModel2.class, references = {@ForeignKeyReference(columnName = "name",
            columnType = String.class, foreignKeyColumnName = "name")})
    String name;

    @PrimaryKey
    @ForeignKey(tableClass = TestModel2.class, references = {@ForeignKeyReference(columnName = "order",
            columnType = int.class, foreignKeyColumnName = "order")})
    int order;
}
