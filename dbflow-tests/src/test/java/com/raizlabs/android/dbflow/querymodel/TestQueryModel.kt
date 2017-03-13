package com.raizlabs.android.dbflow.querymodel

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.QueryModel
import com.raizlabs.android.dbflow.TestDatabase

/**
 * Description:
 */
@QueryModel(database = TestDatabase::class)
data class TestQueryModel(@Column var newName: String? = null,
                          @Column var average_salary: Long = 0,
                          @Column var department: String? = null)
