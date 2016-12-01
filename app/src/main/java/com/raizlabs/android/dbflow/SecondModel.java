package com.raizlabs.android.dbflow;

import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.container.ForeignKeyContainer;

/**
 * Description:
 */
@Table(database = SecondAppDatabase.class)
public class SecondModel extends BaseModel {
    @PrimaryKey
    String name;

    @ForeignKey
    ForeignKeyContainer<SecondModel> secondModel;
}
