package com.raizlabs.android.dbflow.sql.special

import com.raizlabs.android.dbflow.annotation.*
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.TestDatabase
import java.util.*

@Table(name = MatchEntity.NAME, database = TestDatabase::class)
class MatchEntity : BaseModel {

    @Column(name = "ID")
    @PrimaryKey(autoincrement = true)
    var id: Long? = null

    @NotNull(onNullConflict = ConflictAction.ROLLBACK)
    @Column(name = "DATE")
    var date: Date? = null

    @NotNull(onNullConflict = ConflictAction.ROLLBACK)
    @ForeignKey(saveForeignKeyModel = false, onDelete = ForeignKeyAction.CASCADE,
        references = arrayOf(ForeignKeyReference(columnName = "PLAYER_ONE_ID",
            foreignKeyColumnName = "ID")))
    var playerOne: PlayerEntity? = null

    @NotNull(onNullConflict = ConflictAction.ROLLBACK)
    @ForeignKey(onDelete = ForeignKeyAction.CASCADE,
        references = arrayOf(ForeignKeyReference(columnName = "PLAYER_TWO_ID",
            foreignKeyColumnName = "ID")))
    var playerTwo: PlayerEntity? = null

    @ForeignKey(onDelete = ForeignKeyAction.CASCADE,
        references = arrayOf(ForeignKeyReference(columnName = "PLAYER_WINNER_ID",
            foreignKeyColumnName = "ID")))
    var playerWinner: PlayerEntity? = null

    constructor(date: Date, playerOne: PlayerEntity, playerTwo: PlayerEntity) {
        this.date = date
        this.playerOne = playerOne
        this.playerTwo = playerTwo
    }

    internal constructor() {
    }

    companion object {

        const val NAME = "MATCH"
    }
}
