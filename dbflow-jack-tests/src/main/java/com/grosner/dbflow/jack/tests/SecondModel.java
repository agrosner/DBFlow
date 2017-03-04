package com.grosner.dbflow.jack.tests;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Description:
 */
@Table(database = SecondAppDatabase.class)
public class SecondModel extends BaseModel {

    @Column
    @PrimaryKey
    String name;
}
