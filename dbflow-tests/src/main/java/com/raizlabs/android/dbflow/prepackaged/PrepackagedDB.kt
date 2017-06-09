package com.raizlabs.android.dbflow.prepackaged

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.Database
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel

@Database(name = PrepackagedDB.NAME, version = PrepackagedDB.VERSION)
object PrepackagedDB {

    const val NAME = "prepackaged"

    const val VERSION = 1
}

@Table(database = PrepackagedDB::class, allFields = true)
class Dog : BaseModel() {

    @PrimaryKey
    var id: Int = 0

    @Column
    var breed: String? = null

    @Column
    var color: String? = null
}