package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.annotation.ColumnIgnore;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

import java.util.Date;

@Table(database = TestDatabase.class, allFields = true)
public class Activity extends BaseModel {

    @PrimaryKey(autoincrement = true)
    int id;

    Date date;

    int steps;

    @ColumnIgnore
    private double calories;

}
