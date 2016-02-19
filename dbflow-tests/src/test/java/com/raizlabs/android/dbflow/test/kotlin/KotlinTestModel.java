package com.raizlabs.android.dbflow.test.kotlin;

import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Description:
 */
@Table(database = KotlinTestDatabase.class)
public class KotlinTestModel extends BaseModel {

    @PrimaryKey(autoincrement = true)
    int id;
}
