package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.data.Blob;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

import java.util.Date;

/**
 * Description:
 */
@ModelContainer
@Table(database = TestDatabase.class)
public class BlobModel extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    int key;

    @Column(name = "image_blob")
    Blob blob;

    @Column
    Date date;
}
