package com.dbflow5.models.java;

import com.dbflow5.annotation.Column;
import com.dbflow5.annotation.ForeignKey;
import com.dbflow5.annotation.Table;

@Table
public class ExampleModel extends DatabaseModel {
    @Column
    String name;

    @ForeignKey
    JavaModel model;
}
