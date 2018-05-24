package com.dbflow5.prepackaged

import com.dbflow5.annotation.Column
import com.dbflow5.annotation.Database
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table
import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.structure.BaseModel

@Database(version = 1)
abstract class PrepackagedDB : DBFlowDatabase()

@Table(database = PrepackagedDB::class, allFields = true)
class Dog : BaseModel() {

    @PrimaryKey
    var id: Int = 0

    @Column
    var breed: String? = null

    @Column
    var color: String? = null
}