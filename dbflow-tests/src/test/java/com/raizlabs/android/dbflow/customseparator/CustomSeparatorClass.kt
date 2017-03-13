package com.raizlabs.android.dbflow.customseparator

import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel

/**
 * Description:
 */
@Table(database = CustomSeparatorDatabase::class)
class CustomSeparatorClass : BaseModel() {

    @PrimaryKey
    var id: Int = 0
}
