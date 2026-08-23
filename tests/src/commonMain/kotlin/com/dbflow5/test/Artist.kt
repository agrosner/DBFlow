package com.dbflow5.test

import com.dbflow5.annotation.ManyToMany
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table

@ManyToMany(referencedTable = Song::class)
@Table
data class Artist(
    @PrimaryKey(autoincrement = true)
    val id: Int = 0,
    val name: String,
)