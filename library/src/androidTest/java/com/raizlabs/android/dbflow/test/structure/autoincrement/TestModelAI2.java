package com.raizlabs.android.dbflow.test.structure.autoincrement;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ContainerAdapter;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description:
 */
@ContainerAdapter
@Table(databaseName = TestDatabase.NAME)
public class TestModelAI2 extends BaseModel {

    @Column(columnType = Column.PRIMARY_KEY_AUTO_INCREMENT, name = "_id")
    Long id;

    @Column
    String name;
}
