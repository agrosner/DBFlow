package com.raizlabs.android.dbflow.test.global;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Description:
 */
@Table(database = GlobalDatabase.class, updateConflict = ConflictAction.IGNORE)
public class GlobalModel extends BaseModel {

    @Column
    @PrimaryKey(rowID = true)
    int id;

    @Column
    String name;
}
