package com.raizlabs.android.dbflow.test.sql.special

import com.raizlabs.android.dbflow.annotation.*
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.test.TestDatabase

@Table(name = ScoreEntity.NAME, database = TestDatabase::class)
class ScoreEntity : BaseModel {

    @Column(name = "ID")
    @PrimaryKey(autoincrement = true)
    var id: Long? = null

    @NotNull(onNullConflict = ConflictAction.ROLLBACK)
    @ForeignKey(onDelete = ForeignKeyAction.CASCADE,
        references = arrayOf(ForeignKeyReference(columnName = "GAME_ID",
            foreignKeyColumnName = "ID")))
    var game: GameEntity? = null

    @NotNull(onNullConflict = ConflictAction.ROLLBACK)
    @ForeignKey(onDelete = ForeignKeyAction.CASCADE,
        references = arrayOf(ForeignKeyReference(columnName = "PLAYER_ID",
            foreignKeyColumnName = "ID")))
    var player: PlayerEntity? = null

    @Column(name = "SCORE")
    var score: Int = 0

    constructor(game: GameEntity, player: PlayerEntity, score: Int) {
        this.game = game
        this.player = player
        this.score = score

    }

    constructor() {
    }

    companion object {

        const val NAME = "SCORE"
    }
}
