package com.raizlabs.android.dbflow.test.structure

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.test.TestDatabase

@Table(database = TestDatabase::class)
open class TestModel1 : BaseModel() {
    @Column
    @PrimaryKey
    var name: String? = null

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val that = o as TestModel1?

        return !if (name != null) name != that!!.name else that!!.name != null

    }

    override fun hashCode(): Int {
        return if (name != null) name!!.hashCode() else 0
    }
}
