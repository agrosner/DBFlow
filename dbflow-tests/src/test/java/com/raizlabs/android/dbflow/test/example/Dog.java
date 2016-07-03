package com.raizlabs.android.dbflow.test.example;

import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.container.ForeignKeyContainer;
import com.raizlabs.android.dbflow.test.TestDatabase;

@ModelContainer
@Table(database = TestDatabase.class)
public class Dog extends BaseModel {

    @PrimaryKey
    String name;

    @ForeignKey
    @PrimaryKey
    ForeignKeyContainer<Breed> breed; // tableClass only needed for single-field refs that are not Model.

    @ForeignKey
    ForeignKeyContainer<Owner> owner;

    public void associateOwner(Owner owner) {
        this.owner = FlowManager.getContainerAdapter(Owner.class).toForeignKeyContainer(owner); // convenience conversion
    }

    public void associateBreed(Breed breed) {
        this.breed = FlowManager.getContainerAdapter(Breed.class).toForeignKeyContainer(breed); // convenience conversion
    }
}
