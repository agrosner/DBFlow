package com.raizlabs.android.dbflow.test.structure.join;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description: Taken from http://www.tutorialspoint.com/sqlite/sqlite_using_joins.htm
 */
@Table(database = TestDatabase.class)
public class Department extends BaseModel {

    @Column
    @PrimaryKey
    long id;

    @Column
    String dept;

    @Column
    long emp_id;
}
