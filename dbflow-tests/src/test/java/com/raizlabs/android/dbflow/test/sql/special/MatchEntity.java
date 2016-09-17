package com.raizlabs.android.dbflow.test.sql.special;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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

import java.util.Date;

@Table(name = MatchEntity.NAME, database = TestDatabase.class)
public class MatchEntity extends BaseModel {

    public static final String NAME = "MATCH";

    @Column(name = "ID")
    @PrimaryKey(autoincrement = true)
    Long id;

    @NotNull(onNullConflict = ConflictAction.ROLLBACK)
    @Column(name = "DATE")
    Date date;

    @NotNull(onNullConflict = ConflictAction.ROLLBACK)
    @ForeignKey(saveForeignKeyModel = false,
            onDelete = ForeignKeyAction.CASCADE,
            references = @ForeignKeyReference(columnName = "PLAYER_ONE_ID",
                    foreignKeyColumnName = "ID",
                    columnType = Long.class,
                    referencedFieldIsPackagePrivate = true))
    PlayerEntity playerOne;

    @NotNull(onNullConflict = ConflictAction.ROLLBACK)
    @ForeignKey(
            onDelete = ForeignKeyAction.CASCADE,
            references = @ForeignKeyReference(columnName = "PLAYER_TWO_ID",
                    foreignKeyColumnName = "ID",
                    columnType = Long.class,
                    referencedFieldIsPackagePrivate = true))
    PlayerEntity playerTwo;

    @ForeignKey(
            onDelete = ForeignKeyAction.CASCADE,
            references = @ForeignKeyReference(columnName = "PLAYER_WINNER_ID",
                    foreignKeyColumnName = "ID", columnType = long.class))
    PlayerEntity playerWinner;

    public MatchEntity(@NonNull final Date date, @NonNull final PlayerEntity playerOne, @NonNull final PlayerEntity playerTwo) {
        this.date = date;
        setPlayerOne(playerOne);
        setPlayerTwo(playerTwo);
    }

    MatchEntity() {
    }

    @NonNull
    public Long getId() {
        return id;
    }

    @NonNull
    public Date getDate() {
        return date;
    }

    public void setDate(@NonNull final Date date) {
        this.date = date;
    }

    @NonNull
    @SuppressWarnings("ConstantConditions")
    public PlayerEntity getPlayerOne() {
        return this.playerOne.load();
    }

    public void setPlayerOne(@NonNull final PlayerEntity player) {
        this.playerOne = player;
    }

    @NonNull
    @SuppressWarnings("ConstantConditions")
    public PlayerEntity getPlayerTwo() {
        return this.playerTwo.load();
    }

    public void setPlayerTwo(@NonNull final PlayerEntity player) {
        this.playerTwo = player;
    }

    @Nullable
    @SuppressWarnings("ConstantConditions")
    public PlayerEntity getPlayerWinner() {
        return this.playerWinner;
    }

    public void setPlayerWinner(@Nullable final PlayerEntity player) {
        this.playerWinner = player;
    }
}
