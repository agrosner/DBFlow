package com.raizlabs.android.dbflow.test.sql.special;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyAction;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

@Table(name = ScoreEntity.NAME, database = TestDatabase.class)
public class ScoreEntity extends BaseModel {

    public static final String NAME = "SCORE";

    @Column(name = "ID")
    @PrimaryKey(autoincrement = true)
    Long id;

    @NotNull(onNullConflict = ConflictAction.ROLLBACK)
    @ForeignKey(
            onDelete = ForeignKeyAction.CASCADE,
            references = @ForeignKeyReference(columnName = "GAME_ID",
                    foreignKeyColumnName = "ID",
                    columnType = Long.class,
                    referencedFieldIsPackagePrivate = true))
    GameEntity game;

    @NotNull(onNullConflict = ConflictAction.ROLLBACK)
    @ForeignKey(
            onDelete = ForeignKeyAction.CASCADE,
            references = @ForeignKeyReference(columnName = "PLAYER_ID",
                    foreignKeyColumnName = "ID",
                    columnType = Long.class,
                    referencedFieldIsPackagePrivate = true))
    PlayerEntity player;

    @Column(name = "SCORE")
    int score;

    public ScoreEntity(@NonNull final GameEntity game, @NonNull final PlayerEntity player, final int score) {
        setGame(game);
        setPlayer(player);
        this.score = score;
    }

    ScoreEntity() {
    }

    @NonNull
    public Long getId() {
        return id;
    }

    @NonNull
    @SuppressWarnings("ConstantConditions")
    public GameEntity getGame() {
        return this.game;
    }

    public void setGame(@NonNull final GameEntity game) {
        this.game = game;
    }

    @NonNull
    @SuppressWarnings("ConstantConditions")
    public PlayerEntity getPlayer() {
        return this.player;
    }

    public void setPlayer(@NonNull final PlayerEntity player) {
        this.player = player;
    }

    public int getScore() {
        return score;
    }

    public void setScore(final int score) {
        this.score = score;
    }
}
