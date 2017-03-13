package com.raizlabs.android.dbflow.sql

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.structure.TestModel1

@Table(database = TestDatabase::class)
class ConditionModel : TestModel1() {
    @Column
    var number: Long = 0

    @Column
    var bytes: Int = 0

    @Column
    var fraction: Double = 0.toDouble()

    @Column
    var floatie: Float = 0.toFloat()

    @Column
    var shortie: Short = 0

    @Column
    var bytie: Byte = 0

    @Column
    var charie: Char = ' '
}
