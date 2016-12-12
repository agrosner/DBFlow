package com.raizlabs.android.dbflow.test.structure.join

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.QueryModel
import com.raizlabs.android.dbflow.structure.BaseQueryModel
import com.raizlabs.android.dbflow.test.TestDatabase

/**
 * Description: Represents the join of two tables of [Company] and [Department].
 */
@QueryModel(database = TestDatabase::class)
class CompanyDepartmentJoin : BaseQueryModel() {

    @Column
    var emp_id: Long = 0

    @Column
    var name: String? = null

    @Column
    var dept: String? = null
}
