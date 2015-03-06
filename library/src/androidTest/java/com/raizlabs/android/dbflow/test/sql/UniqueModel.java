package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.UniqueGroup;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description:
 */
@Table(databaseName = TestDatabase.NAME,
        uniqueColumnGroups = {@UniqueGroup(groupNumber = 1, uniqueConflict = ConflictAction.ROLLBACK),
        @UniqueGroup(groupNumber = 2, uniqueConflict = ConflictAction.REPLACE)})
public class UniqueModel extends BaseModel {

    @Column(columnType = Column.PRIMARY_KEY_AUTO_INCREMENT)
    long id;

    @Column(unique = true, uniqueGroups = 1, onUniqueConflict = ConflictAction.REPLACE)
    String uniqueName;

    @Column(unique = true, uniqueGroups = 2, onUniqueConflict = ConflictAction.ROLLBACK)
    String anotherUnique;

    @Column(unique = true, uniqueGroups = {1, 2})
    String sharedUnique;
}
