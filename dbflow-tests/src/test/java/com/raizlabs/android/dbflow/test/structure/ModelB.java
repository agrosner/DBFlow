package com.raizlabs.android.dbflow.test.structure;

import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description:
 */
@Table(database = TestDatabase.class)
@ModelContainer
public class ModelB extends BaseModel {
    @ForeignKey
    @PrimaryKey
    public ModelA modelA;
    @PrimaryKey
    public int subID;
}
