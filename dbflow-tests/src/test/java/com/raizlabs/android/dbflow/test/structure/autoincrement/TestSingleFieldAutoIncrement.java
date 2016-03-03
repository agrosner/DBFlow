package com.raizlabs.android.dbflow.test.structure.autoincrement;

import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description:
 */
@Table(database = TestDatabase.class)
public class TestSingleFieldAutoIncrement extends BaseModel {

    @PrimaryKey(autoincrement = true)
    long id;
}
