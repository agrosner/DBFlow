package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.Unique;
import com.raizlabs.android.dbflow.annotation.UniqueGroup;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

@Table(database = TestDatabase.class,
        uniqueColumnGroups = {@UniqueGroup(groupNumber = 1, uniqueConflict = ConflictAction.FAIL),
                @UniqueGroup(groupNumber = 2, uniqueConflict = ConflictAction.ROLLBACK)})
public class UniqueModel2 extends BaseModel {

    @Column
    @PrimaryKey
    @Unique(unique = false, uniqueGroups = {1, 2})
    String name;

    @Column
    @Unique(unique = false, uniqueGroups = 1)
    String number;

    @Column
    @Unique(unique = false, uniqueGroups = 2)
    String address;

}
