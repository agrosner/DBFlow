package com.raizlabs.android.dbflow.test.example

import com.raizlabs.android.dbflow.annotation.ForeignKey
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.test.TestDatabase

@Table(database = TestDatabase::class)
class Dog : BaseModel() {

    @PrimaryKey
    var name: String? = null

    @ForeignKey
    @PrimaryKey
    var breed: Breed // tableClass only needed for single-field refs that are not Model.

    @ForeignKey
    var owner: Owner

    fun associateOwner(owner: Owner) {
        this.owner = owner // convenience conversion
    }

    fun associateBreed(breed: Breed) {
        this.breed = breed // convenience conversion
    }
}
