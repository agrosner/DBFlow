package com.raizlabs.android.dbflow.querymodel

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.TestDatabase

/**
 * Description:
 */
@Table(database = TestDatabase::class)
class SalaryModel : BaseModel() {

    @Column
    @PrimaryKey
    var uid: String? = null

    @Column
    var salary: Long = 0

    @Column
    var name: String? = null

    @Column
    var department: String? = null
}
