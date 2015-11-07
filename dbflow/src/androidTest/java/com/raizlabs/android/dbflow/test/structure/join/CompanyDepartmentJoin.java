package com.raizlabs.android.dbflow.test.structure.join;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.QueryModel;
import com.raizlabs.android.dbflow.structure.BaseQueryModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description: Represents the join of two tables of {@link Company} and {@link Department}.
 */
@QueryModel(database = TestDatabase.class)
public class CompanyDepartmentJoin extends BaseQueryModel {

    @Column
    String emp_id;

    @Column
    String name;

    @Column
    String dept;
}
