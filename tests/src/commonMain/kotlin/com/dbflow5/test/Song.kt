package com.dbflow5.test

import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table

@Table
data class Song(
    @PrimaryKey(autoincrement = true)
    val id: Int = 0,
    val name: String,
)
