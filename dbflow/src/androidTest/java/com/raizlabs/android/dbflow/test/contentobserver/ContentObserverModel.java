package com.raizlabs.android.dbflow.test.contentobserver;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description:
 */
@Table(database = TestDatabase.class)
public class ContentObserverModel extends BaseModel {

    @Column
    @PrimaryKey
    String name;

    @Column
    @PrimaryKey
    int id;

    @Column
    String somethingElse;

}
