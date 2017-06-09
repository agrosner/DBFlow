package com.raizlabs.android.dbflow.models.java;

import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.structure.BaseModel;

public class DatabaseModel extends BaseModel {
    @PrimaryKey
    Integer id;
}