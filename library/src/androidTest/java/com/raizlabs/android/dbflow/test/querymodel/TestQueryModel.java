package com.raizlabs.android.dbflow.test.querymodel;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.QueryModel;
import com.raizlabs.android.dbflow.structure.BaseQueryModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description:
 */
@QueryModel(databaseName = TestDatabase.NAME)
public class TestQueryModel extends BaseQueryModel {

    @Column
    String newName;

    @Column
    long average_salary;

    @Column
    String department;
}
