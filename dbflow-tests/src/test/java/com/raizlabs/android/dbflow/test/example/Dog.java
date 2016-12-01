package com.raizlabs.android.dbflow.test.example;

import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

@Table(database = TestDatabase.class)
public class Dog extends BaseModel {

    @PrimaryKey
    String name;

    @ForeignKey
    @PrimaryKey
    Breed breed; // tableClass only needed for single-field refs that are not Model.

    @ForeignKey
    Owner owner;

    public void associateOwner(Owner owner) {
        this.owner = owner; // convenience conversion
    }

    public void associateBreed(Breed breed) {
        this.breed = breed; // convenience conversion
    }
}
