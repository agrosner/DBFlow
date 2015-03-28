package com.raizlabs.android.dbflow.test.kotlin;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Description:
 */
@Table(databaseName = KotlinDatabase.NAME)
public class KotlinClass extends BaseModel {

    @Column
    @PrimaryKey
    String name;
}
