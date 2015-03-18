package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.UniqueGroup;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

@Table(databaseName = TestDatabase.NAME,
        uniqueColumnGroups = {@UniqueGroup(groupNumber = 1, uniqueConflict = ConflictAction.FAIL),
                @UniqueGroup(groupNumber = 2, uniqueConflict = ConflictAction.ROLLBACK)})
public class UniqueModel2 extends BaseModel {

    @Column(columnType = Column.PRIMARY_KEY, uniqueGroups = {1, 2})
    String name;

    @Column(uniqueGroups = 1)
    String number;

    @Column(uniqueGroups = 2)
    String address;

}
