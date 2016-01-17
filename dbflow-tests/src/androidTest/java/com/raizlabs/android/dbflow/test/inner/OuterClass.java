package com.raizlabs.android.dbflow.test.inner;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description:
 */
public class OuterClass {

    @Table(database = TestDatabase.class)
    public static class InnerClass extends BaseModel {

        @PrimaryKey
        long id;

        @Column
        String column;
    }
}
