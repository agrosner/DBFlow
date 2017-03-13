package com.raizlabs.android.dbflow.structure

import android.location.Location

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.TestDatabase

@Table(database = TestDatabase::class)
class TestPrimaryWhere : TestModel1() {
    @Column
    @PrimaryKey
    var location: Location? = null
}
