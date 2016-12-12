package com.raizlabs.android.dbflow.test.customhelper

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel

/**
 * Description:
 */
@Table(database = HelperDatabase::class)
class HelperModel : BaseModel() {

    @Column
    @PrimaryKey
    var name: String? = null
}
