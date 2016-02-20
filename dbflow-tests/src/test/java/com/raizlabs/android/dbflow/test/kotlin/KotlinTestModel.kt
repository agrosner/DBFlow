package com.raizlabs.android.dbflow.test.kotlin

import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel

/**
 * Description:
 */
@Table(database = KotlinTestDatabase::class)
class KotlinTestModel : BaseModel() {

    @PrimaryKey(autoincrement = true)
    var id: Int = 0
}
