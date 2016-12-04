package com.raizlabs.android.dbflow.test.sql

import com.raizlabs.android.dbflow.annotation.ColumnIgnore
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.test.TestDatabase

import java.util.Date

@Table(database = TestDatabase::class, allFields = true)
class Activity : BaseModel() {

    @PrimaryKey(autoincrement = true)
    var id: Int = 0

    var date: Date? = null

    var steps: Int = 0

    @ColumnIgnore
    private val calories: Double = 0.toDouble()

}
