package com.raizlabs.dbflow5.dbflow.models.java.otherpackage;

import com.raizlabs.dbflow5.dbflow.TestDatabase;
import com.raizlabs.dbflow5.annotation.Column;
import com.raizlabs.dbflow5.annotation.Table;
import com.raizlabs.dbflow5.dbflow.models.java.DatabaseModel;

@Table(database = TestDatabase.class)
public class ExampleModel extends DatabaseModel {
    @Column
    String name;
}
