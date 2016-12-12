package com.raizlabs.android.dbflow.test.example

import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.test.TestDatabase

/**
 * Description:
 */
@Table(database = TestDatabase::class)
class Breed : BaseModel() {

    @PrimaryKey
    var breed_id: Long = 0
}
