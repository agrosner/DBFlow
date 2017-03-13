package com.raizlabs.android.dbflow.kotlin

import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.TestDatabase

@Table(database = TestDatabase::class)
class Account : BaseModel() {

    @PrimaryKey
    var id: Int = 0
}