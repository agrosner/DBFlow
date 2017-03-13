package com.raizlabs.android.dbflow.sql

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.data.Blob
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.TestDatabase

import java.util.Date

/**
 * Description:
 */
@Table(database = TestDatabase::class)
class BlobModel : BaseModel() {

    @PrimaryKey
    var key: Int = 0

    @Column(name = "image_blob")
    var blob: Blob? = null

    @Column
    var date: Date? = null
}
