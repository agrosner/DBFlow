package com.raizlabs.android.dbflow.test.structure.backup;

import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Description:
 */
@Table(database = BackupDatabase.class)
public class BackupModel extends BaseModel {

    @PrimaryKey
    String name;

    public String getName() {
        return name;
    }
}
