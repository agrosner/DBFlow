package com.raizlabs.android.dbflow.sql.special

import com.raizlabs.android.dbflow.annotation.*
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.TestDatabase

@Table(name = PlayerEntity.NAME, database = TestDatabase::class)
class PlayerEntity : BaseModel {

    @Column(name = "ID")
    @PrimaryKey(autoincrement = true)
    var id: Long? = null

    @NotNull(onNullConflict = ConflictAction.ROLLBACK)
    @Column(name = "NAME")
    var name: String = ""

    constructor(name: String) {
        this.name = name
    }

    constructor() {
    }

    companion object {

        const val NAME = "PLAYER"
    }
}
