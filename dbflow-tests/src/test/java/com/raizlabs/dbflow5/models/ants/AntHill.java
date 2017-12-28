package com.raizlabs.dbflow5.models.ants;

import com.raizlabs.dbflow5.TestDatabase;
import com.raizlabs.dbflow5.annotation.PrimaryKey;
import com.raizlabs.dbflow5.annotation.Table;
import com.raizlabs.dbflow5.structure.BaseModel;

@Table(database = TestDatabase.class)
public class AntHill extends BaseModel {
    @PrimaryKey
    public String hillId;
}

