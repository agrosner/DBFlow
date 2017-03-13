package com.raizlabs.android.dbflow.structure

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.TestDatabase

/**
 * Description:
 */
@Table(database = TestDatabase::class)
class ModelA : BaseModel() {
    @Column
    @PrimaryKey
    var id: Int = 0
    @Column
    var Name: String? = null
}
