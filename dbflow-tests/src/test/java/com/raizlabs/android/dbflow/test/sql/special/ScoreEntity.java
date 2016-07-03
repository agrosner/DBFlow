package com.raizlabs.android.dbflow.test.sql.special;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyAction;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.container.ForeignKeyContainer;
import com.raizlabs.android.dbflow.test.TestDatabase;

@ModelContainer
@Table(name = ScoreEntity.NAME, database = TestDatabase.class)
public class ScoreEntity extends BaseModel {

    public static final String NAME = "SCORE";

    @Column(name = "ID")
    @PrimaryKey(autoincrement = true)
    Long id;

    @NotNull(onNullConflict = ConflictAction.ROLLBACK)
    @ForeignKey(saveForeignKeyModel = false,
            onDelete = ForeignKeyAction.CASCADE,
            references = @ForeignKeyReference(columnName = "GAME_ID",
                    foreignKeyColumnName = "ID",
                    columnType = Long.class,
                    referencedFieldIsPackagePrivate = true))
    ForeignKeyContainer<GameEntity> game;

    @NotNull(onNullConflict = ConflictAction.ROLLBACK)
    @ForeignKey(saveForeignKeyModel = false,
            onDelete = ForeignKeyAction.CASCADE,
            references = @ForeignKeyReference(columnName = "PLAYER_ID",
                    foreignKeyColumnName = "ID",
                    columnType = Long.class,
                    referencedFieldIsPackagePrivate = true))
    ForeignKeyContainer<PlayerEntity> player;

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
        return this.game.load();
    }

    public void setGame(@NonNull final GameEntity game) {
        this.game = FlowManager.getContainerAdapter(GameEntity.class).toForeignKeyContainer(game);
    }

    @NonNull
    @SuppressWarnings("ConstantConditions")
    public PlayerEntity getPlayer() {
        return this.player.load();
    }

    public void setPlayer(@NonNull final PlayerEntity player) {
        this.player = FlowManager.getContainerAdapter(PlayerEntity.class).toForeignKeyContainer(player);
    }

    public int getScore() {
        return score;
    }

    public void setScore(final int score) {
        this.score = score;
    }
}
