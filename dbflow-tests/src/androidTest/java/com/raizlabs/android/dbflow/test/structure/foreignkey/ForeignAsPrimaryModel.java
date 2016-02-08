package com.raizlabs.android.dbflow.test.structure.foreignkey;

import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.test.TestDatabase;
import com.raizlabs.android.dbflow.test.sql.BoxedModel;
import com.raizlabs.android.dbflow.test.structure.TestModel1;

/**
 * Description:
 */
@Table(database = TestDatabase.class)
public class ForeignAsPrimaryModel extends BaseModel {

    @PrimaryKey
    long id;

    @ForeignKey
    @PrimaryKey
    TestModel1 testModel1;

    @ForeignKey
    @PrimaryKey
    BoxedModel boxedModel;
}
