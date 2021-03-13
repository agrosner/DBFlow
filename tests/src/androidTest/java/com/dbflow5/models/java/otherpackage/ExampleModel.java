package com.dbflow5.models.java.otherpackage;

import com.dbflow5.TestDatabase;
import com.dbflow5.annotation.Column;
import com.dbflow5.annotation.ForeignKey;
import com.dbflow5.annotation.Table;
import com.dbflow5.models.java.DatabaseModel;
import com.dbflow5.models.java.JavaModel;

@Table(database = TestDatabase.class)
public class ExampleModel extends DatabaseModel {
    @Column
    String name;

    @ForeignKey
    JavaModel model;
}
