package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.Index;
import com.raizlabs.android.dbflow.annotation.IndexGroup;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description:
 */
@Table(databaseName = TestDatabase.NAME, indexGroups = @IndexGroup(name = "customIndex", unique = true))
public class IndexModel extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    long id;

    @Index
    @Column
    String name;

    @Index
    @Column
    long salary;

}
