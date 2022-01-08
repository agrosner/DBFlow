package com.dbflow5.models

import com.dbflow5.annotation.Column
import com.dbflow5.annotation.ManyToMany
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table

@ManyToMany(referencedTable = Song::class)
@Table
class Artist(
    @PrimaryKey(autoincrement = true) var id: Int = 0,
    @Column var name: String = ""
)

@Table
class Song(
    @PrimaryKey(autoincrement = true) var id: Int = 0,
    @Column var name: String = ""
)