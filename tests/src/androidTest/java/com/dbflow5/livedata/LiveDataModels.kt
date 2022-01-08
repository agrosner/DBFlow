package com.dbflow5.livedata

import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table

/**
 * Description: Basic live data object model.
 */
@Table
data class LiveDataModel(
    @PrimaryKey var id: String = "",
    var name: Int = 0
)
