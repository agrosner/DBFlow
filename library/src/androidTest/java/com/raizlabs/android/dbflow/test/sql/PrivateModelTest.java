package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description:
 */
@Table(databaseName = TestDatabase.NAME)
public class PrivateModelTest extends BaseModel {

    @Column
    @PrimaryKey
    private String name;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
