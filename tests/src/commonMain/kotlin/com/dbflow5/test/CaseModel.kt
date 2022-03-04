package com.dbflow5.test

import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table

@Table
data class CaseModel(
    @PrimaryKey val id: Int,
    val country: String,
)
