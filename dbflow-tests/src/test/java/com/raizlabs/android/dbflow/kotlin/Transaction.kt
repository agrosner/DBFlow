package com.raizlabs.android.dbflow.kotlin

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.ForeignKey
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.TestDatabase

@Table(database = TestDatabase::class)
class Transaction(from: Account? = null, value: Float = 0.0f, to: Account? = null, id: Long = -1) : BaseModel() {

    @PrimaryKey(autoincrement = true)
    var id: Long = -1

    @ForeignKey
    var from: Account? = null

    @Column
    var value: Float = 0.0f

    @ForeignKey
    var to: Account? = null
}
