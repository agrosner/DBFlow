package com.dbflow5.models.java;

import com.dbflow5.annotation.PrimaryKey;

public class DatabaseModel {
    @PrimaryKey
    private Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}