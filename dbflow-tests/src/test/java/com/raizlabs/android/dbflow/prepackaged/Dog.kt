package com.raizlabs.android.dbflow.prepackaged

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel

@Table(database = TestDB::class)
class Dog : BaseModel() {

    @PrimaryKey
    var id: Int = 0

    @Column
    var breed: String? = null

    @Column
    var color: String? = null
}
