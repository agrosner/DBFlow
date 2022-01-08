package com.dbflow5.contentobserver

import com.dbflow5.annotation.Column
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table

@Table
class User(
    @PrimaryKey var id: Int = 0,
    @PrimaryKey var name: String = "",
    @Column var age: Int = 0
)
