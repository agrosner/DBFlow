package com.raizlabs.android.dbflow.test.sql.special

import com.raizlabs.android.dbflow.annotation.*
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.test.TestDatabase

@Table(name = GameEntity.NAME, database = TestDatabase::class)
class GameEntity : BaseModel {

    @Column(name = "ID")
    @PrimaryKey(autoincrement = true)
    var id: Long? = null

    @NotNull(onNullConflict = ConflictAction.ROLLBACK)
    @ForeignKey(onDelete = ForeignKeyAction.CASCADE)
    var match: MatchEntity? = null

    constructor(match: MatchEntity) {
        this.match = match
    }

    internal constructor() {
    }

    companion object {

        const val NAME = "GAME"
    }
}
