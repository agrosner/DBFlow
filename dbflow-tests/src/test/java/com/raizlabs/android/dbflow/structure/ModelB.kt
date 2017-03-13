package com.raizlabs.android.dbflow.structure

import com.raizlabs.android.dbflow.annotation.ForeignKey
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.TestDatabase

/**
 * Description:
 */
@Table(database = TestDatabase::class)
class ModelB : BaseModel() {
    @ForeignKey
    @PrimaryKey
    var modelA: ModelA? = null
    @PrimaryKey
    var subID: Int = 0
}
