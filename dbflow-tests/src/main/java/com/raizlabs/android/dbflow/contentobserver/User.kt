package com.raizlabs.android.dbflow.contentobserver

import com.raizlabs.android.dbflow.AppDatabase
import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table

@Table(database = AppDatabase::class)
class User(@PrimaryKey var id: Int = 0, @PrimaryKey var name: String = "", @Column var age: Int = 0)