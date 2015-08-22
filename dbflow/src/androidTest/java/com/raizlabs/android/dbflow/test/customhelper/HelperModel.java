package com.raizlabs.android.dbflow.test.customhelper;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Description:
 */
@Table(databaseName = HelperDatabase.NAME)
public class HelperModel extends BaseModel {

    @Column
    @PrimaryKey
    String name;
}
