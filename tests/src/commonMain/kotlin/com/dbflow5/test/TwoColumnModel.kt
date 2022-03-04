package com.dbflow5.test

import com.dbflow5.annotation.Column
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table

@Table
data class TwoColumnModel(
    @PrimaryKey val name: String?,
    @Column(defaultValue = "56") val id: Int,
)
