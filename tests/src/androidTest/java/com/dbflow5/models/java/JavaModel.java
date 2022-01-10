package com.dbflow5.models.java;

import com.dbflow5.annotation.PrimaryKey;
import com.dbflow5.annotation.Table;

@Table
public class JavaModel {

    @PrimaryKey
    String id;

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
