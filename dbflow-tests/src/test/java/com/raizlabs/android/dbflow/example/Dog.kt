package com.raizlabs.android.dbflow.example

import com.raizlabs.android.dbflow.annotation.ForeignKey
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.TestDatabase

@Table(database = TestDatabase::class)
class Dog : BaseModel() {

    @PrimaryKey
    var name: String? = null

    @ForeignKey
    @PrimaryKey
    var breed: Breed? = null

    @ForeignKey
    var owner: Owner? = null

}
