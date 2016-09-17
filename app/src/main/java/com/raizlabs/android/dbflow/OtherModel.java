package com.raizlabs.android.dbflow;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Description:
 */
@Table(name = "OtherModel", database = AppDatabase.class)
public class OtherModel extends BaseModel {

    @Column
    @PrimaryKey
    String name;
}
