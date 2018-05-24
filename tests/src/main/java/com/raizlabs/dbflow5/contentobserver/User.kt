package com.raizlabs.dbflow5.contentobserver

import com.raizlabs.dbflow5.AppDatabase
import com.raizlabs.dbflow5.annotation.Column
import com.raizlabs.dbflow5.annotation.PrimaryKey
import com.raizlabs.dbflow5.annotation.Table

@Table(database = AppDatabase::class)
class User(@PrimaryKey var id: Int = 0, @PrimaryKey var name: String = "", @Column var age: Int = 0)