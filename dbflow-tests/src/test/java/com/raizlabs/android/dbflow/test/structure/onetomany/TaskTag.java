package com.raizlabs.android.dbflow.test.structure.onetomany;

import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

@Table(database = TestDatabase.class)
public class TaskTag extends BaseModel {

    @PrimaryKey(autoincrement = true)
    long id;

    @ForeignKey
    private OneToManyModel2 model;


    public OneToManyModel2 getModelObject() {
        return model;
    }

    public OneToManyModel2 getModel() {
        return model;
    }

    public void setTask(OneToManyModel2 model) {
        this.model = model;
    }

    public void setModel(OneToManyModel2 model) {
        this.model = model;
    }
}
