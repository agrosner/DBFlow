package com.dbflow5.models

import com.dbflow5.TestDatabase
import com.dbflow5.annotation.Column
import com.dbflow5.annotation.ManyToMany
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table

@ManyToMany(referencedTable = Song::class, generateBaseModel = true)
@Table(database = TestDatabase::class)
class Artist(@PrimaryKey(autoincrement = true) var id: Int = 0,
             @Column var name: String = "")

@Table(database = TestDatabase::class)
class Song(@PrimaryKey(autoincrement = true) var id: Int = 0,
           @Column var name: String = "")