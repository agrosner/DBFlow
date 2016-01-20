package com.raizlabs.android.dbflow.test.sqlcipher;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Description:
 */
@Table(database = CipherDatabase.class)
public class CipherModel extends BaseModel {

    @PrimaryKey(autoincrement = true)
    long id;

    @Column
    String name;
}
