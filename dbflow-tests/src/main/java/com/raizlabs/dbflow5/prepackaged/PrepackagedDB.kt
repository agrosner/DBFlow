package com.raizlabs.dbflow5.prepackaged

import com.raizlabs.dbflow5.annotation.Column
import com.raizlabs.dbflow5.annotation.Database
import com.raizlabs.dbflow5.annotation.PrimaryKey
import com.raizlabs.dbflow5.annotation.Table
import com.raizlabs.dbflow5.structure.BaseModel

@Database(version = PrepackagedDB.VERSION)
object PrepackagedDB {
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