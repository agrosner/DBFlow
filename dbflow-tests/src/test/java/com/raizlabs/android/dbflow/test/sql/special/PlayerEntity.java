package com.raizlabs.android.dbflow.test.sql.special;

import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

@ModelContainer
@Table(name = PlayerEntity.NAME, database = TestDatabase.class)
public class PlayerEntity extends BaseModel {

    public static final String NAME = "PLAYER";

    @Column(name = "ID")
    @PrimaryKey(autoincrement = true)
    Long id;
    @NotNull(onNullConflict = ConflictAction.ROLLBACK)
    @Column(name = "NAME")
    String name;

    public PlayerEntity(@NonNull final String name) {
        this.name = name;
    }

    PlayerEntity() {
    }

    @NonNull
    public Long getId() {
        return id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull final String name) {
        this.name = name;
    }
}
