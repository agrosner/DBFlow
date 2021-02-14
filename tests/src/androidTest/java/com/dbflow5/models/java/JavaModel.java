package com.dbflow5.models.java;

import com.dbflow5.TestDatabase;
import com.dbflow5.annotation.PrimaryKey;
import com.dbflow5.annotation.Table;

@Table(database = TestDatabase.class)
public class JavaModel {

    @PrimaryKey
    String id;
}

