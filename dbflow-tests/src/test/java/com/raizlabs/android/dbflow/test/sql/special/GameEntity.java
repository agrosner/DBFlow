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
@Table(name = GameEntity.NAME, database = TestDatabase.class)
public class GameEntity extends BaseModel {

    public static final String NAME = "GAME";

    @Column(name = "ID")
    @PrimaryKey(autoincrement = true)
    Long id;

    @NotNull(onNullConflict = ConflictAction.ROLLBACK)
    @ForeignKey(saveForeignKeyModel = false,
            onDelete = ForeignKeyAction.CASCADE,
            references = @ForeignKeyReference(columnName = "MATCH_ID",
                    foreignKeyColumnName = "ID",
                    columnType = Long.class,
                    referencedFieldIsPackagePrivate = true))
    ForeignKeyContainer<MatchEntity> match;

    public GameEntity(@NonNull final MatchEntity match) {
        setMatch(match);
    }

    GameEntity() {
    }

    @NonNull
    public Long getId() {
        return id;
    }

    @NonNull
    @SuppressWarnings("ConstantConditions")
    public MatchEntity getMatch() {
        return match.load();
    }

    public void setMatch(@NonNull final MatchEntity match) {
        this.match = FlowManager.getContainerAdapter(MatchEntity.class).toForeignKeyContainer(match);
    }
}
