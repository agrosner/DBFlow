package com.raizlabs.android.dbflow.test.example;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Description:
 */
@ModelContainer
@Table(database = ColonyDatabase.class)
public class Ant extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    long id;

    @Column
    String type;

    @Column
    boolean isMale;

    @Column
    @ForeignKey(saveForeignKeyModel = false)
    QueenForeignKeyContainer queenForeignKeyContainer;

    public void associateQueen(Queen queen) {
        queenForeignKeyContainer = new QueenForeignKeyContainer(FlowManager.getContainerAdapter(Queen.class)
                .toForeignKeyContainer(queen));
    }
}
