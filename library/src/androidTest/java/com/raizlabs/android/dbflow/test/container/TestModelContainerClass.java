package com.raizlabs.android.dbflow.test.container;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ContainerAdapter;
import com.raizlabs.android.dbflow.annotation.ContainerKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.test.TestDatabase;
import com.raizlabs.android.dbflow.test.structure.TestModel1;

/**
* Author: andrewgrosner
* Contributors: { }
* Description:
*/
@Table(value = "TestModelContainer", databaseName = TestDatabase.NAME)
@ContainerAdapter
public class TestModelContainerClass extends TestModel1 {

    @Column(columnType = Column.PRIMARY_KEY)
    String party_type;

    @Column
    @ContainerKey("count1")
    int count;

    @Column
    String party_name;

    @Column(columnType = Column.FOREIGN_KEY,
            references = {@ForeignKeyReference(columnName = "testName", columnType = String.class, foreignColumnName = "name")})
    TestModel1 testModel;
}
