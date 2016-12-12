package com.raizlabs.android.dbflow.test.structure

import com.raizlabs.android.dbflow.annotation.ForeignKey
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.test.TestDatabase

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
