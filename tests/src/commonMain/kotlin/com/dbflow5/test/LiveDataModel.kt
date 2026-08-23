package com.dbflow5.test

import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table

/**
 * Description: Basic live data object model.
 */
@Table
data class LiveDataModel(
    @PrimaryKey val id: String,
    val name: Int
)
