package com.raizlabs.android.dbflow.sql

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.TestDatabase

@Table(database = TestDatabase::class)
class EnumModel : BaseModel() {

    enum class Difficulty {
        EASY,
        MEDIUM,
        HARD
    }

    @Column
    @PrimaryKey(autoincrement = true)
    var id: Long = 0

    @Column
    var difficulty: Difficulty? = null
}
