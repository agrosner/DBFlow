package com.raizlabs.android.dbflow.structure.onetomany

import com.raizlabs.android.dbflow.annotation.ForeignKey
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.TestDatabase

@Table(database = TestDatabase::class)
class TaskTag : BaseModel() {

    @PrimaryKey(autoincrement = true)
    var id: Long = 0

    @ForeignKey
    var modelObject: OneToManyModel2? = null

}
