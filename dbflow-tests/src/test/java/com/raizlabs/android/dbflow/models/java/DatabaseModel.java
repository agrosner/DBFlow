package com.raizlabs.android.dbflow.models.java;

import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.structure.BaseModel;

public class DatabaseModel extends BaseModel {
    @PrimaryKey
    private Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}