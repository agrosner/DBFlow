package com.raizlabs.android.dbflow.sql

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.TestDatabase

/**
 * Description:
 */
@Table(database = TestDatabase::class)
class CaseModel : BaseModel() {


    @PrimaryKey
    var customerId: Long = 0

    @Column
    var firstName: String? = null

    @Column
    var lastName: String? = null

    @Column
    var country: String? = null
}
