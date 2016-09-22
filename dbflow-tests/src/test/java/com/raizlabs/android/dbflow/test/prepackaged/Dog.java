package com.raizlabs.android.dbflow.test.prepackaged;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

@Table(database = TestDB.class)
public class Dog extends BaseModel {

    @PrimaryKey
    int id;

    @Column
    String breed;

    @Column
    String color;
}
