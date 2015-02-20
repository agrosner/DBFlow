package com.raizlabs.android.dbflow.test.structure;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description: Tests to ensure the allFields flag works as expected.
 */
@Table(databaseName = TestDatabase.NAME, allFields = true)
public class TestEmptyModel extends BaseModel {

    @Column(columnType = Column.PRIMARY_KEY)
    String name;

    int count;

    boolean truth;

    static int COUNT;

    final String finalName = "";

    private int hidden;
}
