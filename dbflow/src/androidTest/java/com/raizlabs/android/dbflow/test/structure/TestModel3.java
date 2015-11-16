package com.raizlabs.android.dbflow.test.structure;

import com.raizlabs.android.dbflow.annotation.Collate;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

import java.sql.Date;

@Table(database = TestDatabase.class)
public class TestModel3 extends BaseModel {

    @Column
    @PrimaryKey
    public String name;

    @Column(length = 5, collate = Collate.BINARY)
    public int order;

    @Column(collate = Collate.NOCASE)
    public Date date;

    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "testAI_name", foreignKeyColumnName = "name", columnType = String.class)})
    public TestModel1 testAutoIncrement;
}
