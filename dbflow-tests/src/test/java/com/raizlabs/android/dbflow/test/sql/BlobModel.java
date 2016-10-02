package com.raizlabs.android.dbflow.test.sql;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.data.Blob;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.test.TestDatabase;

import java.util.Date;

/**
 * Description:
 */
@Table(database = TestDatabase.class)
public class BlobModel extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    int key;

    @Column(name = "image_blob")
    private Blob blob;

    @Column
    Date date;

    public Blob getBlob() {
        return blob;
    }

    public void setBlob(Blob blob) {
        this.blob = blob;
    }
}
