package com.raizlabs.android.dbflow.test.querymodel;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description:
 */
@Table(databaseName = TestDatabase.NAME)
public class SalaryModel extends BaseModel {

    @Column
    @PrimaryKey
    String uid;

    @Column
    long salary;

    @Column
    String name;

    @Column
    String department;
}
