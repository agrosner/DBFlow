package com.dbflow5.test

import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table

@Table
data class Fts4ContentModel(
    @PrimaryKey(autoincrement = true)
    val id: Int,
    val name: String,
)
