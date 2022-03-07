package com.dbflow5.test

import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table

@Table(orderedCursorLookUp = true)
data class OrderCursorModel(
    val age: Int,
    @PrimaryKey(autoincrement = true)
    val id: Int,
    val name: String?,
)
