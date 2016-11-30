package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.test.TestDatabase;
import com.raizlabs.android.dbflow.test.structure.TestModel1;

import java.util.Date;

@Table(database = TestDatabase.class)
public class DefaultModel extends TestModel1 {

    @Column(defaultValue = "55")
    Integer count;

    @Column(defaultValue = "this is")
    String test;

    @Column(defaultValue = "1000L")
    Date date;

    @Column(defaultValue = "1")
    Boolean aBoolean;

    @Column(defaultValue = "\"\"")
    String emptyString;

}
