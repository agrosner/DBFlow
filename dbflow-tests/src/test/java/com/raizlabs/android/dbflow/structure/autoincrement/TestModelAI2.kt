package com.raizlabs.android.dbflow.structure.autoincrement

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.TestDatabase

@Table(database = TestDatabase::class)
class TestModelAI2 : BaseModel() {

    @Column(name = "_id")
    @PrimaryKey(autoincrement = true)
    var id: Long? = null

    @Column
    var name: String? = null
}
