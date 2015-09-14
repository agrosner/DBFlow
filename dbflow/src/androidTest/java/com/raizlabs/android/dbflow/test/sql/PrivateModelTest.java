package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description:
 */
@Table(database = TestDatabase.class)
public class PrivateModelTest extends BaseModel {

    @Column
    @PrimaryKey
    private String name;

    @Column(getterName = "getThisName", setterName = "setThisName")
    private String anotherName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getThisName() {
        return anotherName;
    }

    public void setThisName(String thisName) {
        this.anotherName = thisName;
    }
}
