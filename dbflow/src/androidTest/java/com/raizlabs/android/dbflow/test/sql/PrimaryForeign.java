package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.annotation.ForeignKey;
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
    @ForeignKey(tableClass = TestModel2.class)
    String name;

}
