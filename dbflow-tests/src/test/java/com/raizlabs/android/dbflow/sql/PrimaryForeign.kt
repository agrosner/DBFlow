package com.raizlabs.android.dbflow.sql

import com.raizlabs.android.dbflow.annotation.ForeignKey
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.structure.TestModel2

/**
 * Description:
 */
@Table(database = TestDatabase::class)
class PrimaryForeign : BaseModel() {

    @PrimaryKey
    @ForeignKey(tableClass = TestModel2::class)
    var name: String? = null

}
