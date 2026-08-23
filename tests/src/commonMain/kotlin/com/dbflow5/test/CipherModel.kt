package com.dbflow5.test

import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table

@Table
data class CipherModel(
    @PrimaryKey(autoincrement = true) val id: Long,
    val name: String?,
)
