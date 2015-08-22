package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

import java.util.Date;

@Table(databaseName = TestDatabase.NAME, allFields = true)
public class Activity extends BaseModel {

    @PrimaryKey(autoincrement = true)
    int id;

    Date date;

    int steps;

    double calories;
}
