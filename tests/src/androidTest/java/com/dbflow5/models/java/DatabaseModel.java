package com.dbflow5.models.java;

import com.dbflow5.annotation.PrimaryKey;
import com.dbflow5.structure.BaseModel;

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