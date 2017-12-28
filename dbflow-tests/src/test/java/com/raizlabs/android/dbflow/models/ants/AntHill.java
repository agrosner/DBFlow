package com.raizlabs.android.dbflow.models.ants;

import com.raizlabs.android.dbflow.TestDatabase;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

@Table(database = TestDatabase.class)
public class AntHill extends BaseModel {
    @PrimaryKey
    public String hillId;
}

