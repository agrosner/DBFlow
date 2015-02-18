package com.raizlabs.android.dbflow.test.provider;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description:
 */
@Table(databaseName = TestDatabase.NAME)
public class ContentProviderModel extends BaseModel {

    @Column(columnType = Column.PRIMARY_KEY_AUTO_INCREMENT)
    long id;

    @Column
    String notes;

    @Column
    String title;
}
