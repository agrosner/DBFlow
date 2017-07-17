package com.raizlabs.android.dbflow.models

import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.ManyToMany
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table

@ManyToMany(referencedTable = Song::class)
@Table(database = TestDatabase::class)
class Artist(@PrimaryKey(autoincrement = true) var id: Int = 0,
             @Column var name: String = "")

@Table(database = TestDatabase::class)
class Song(@PrimaryKey(autoincrement = true) var id: Int = 0,
           @Column var name: String = "")