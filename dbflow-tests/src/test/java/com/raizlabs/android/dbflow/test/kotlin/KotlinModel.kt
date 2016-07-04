package com.raizlabs.android.dbflow.test.kotlin

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel

/**
 * Description:
 */
@Table(database = KotlinDatabase::class)
class KotlinModel() : BaseModel() {
    @PrimaryKey var id: Int = 0

    @Column var name: String? = null
}