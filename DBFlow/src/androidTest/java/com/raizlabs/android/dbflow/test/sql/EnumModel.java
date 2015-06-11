package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description:
 */
@Table(databaseName = TestDatabase.NAME)
@ModelContainer
public class EnumModel extends BaseModel {

    public enum Difficulty {
        EASY,
        MEDIUM,
        HARD
    }

    @Column
    @PrimaryKey(autoincrement = true)
    long id;

    @Column
    Difficulty difficulty;
}
