package com.raizlabs.android.dbflow.structure

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.TestDatabase

/**
 * Description:
 */
@Table(database = TestDatabase::class, allFields = true)
open class TestAllFields : BaseModel() {

    @Column
    @PrimaryKey
    var order: Int = 0
}
