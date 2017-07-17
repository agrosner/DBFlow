package com.raizlabs.android.dbflow.models.java.otherpackage;

import com.raizlabs.android.dbflow.TestDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.models.java.DatabaseModel;

@Table(database = TestDatabase.class)
public class ExampleModel extends DatabaseModel {
    @Column
    String name;
}
