package com.raizlabs.android.dbflow.kotlin

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel

/**
 * Description:
 */
@Table(database = KotlinTestDatabase::class)
class KotlinTestModel : BaseModel() {

    @PrimaryKey(autoincrement = true)
    var id: Int? = null

    @Column
    var name: String? = null
}
