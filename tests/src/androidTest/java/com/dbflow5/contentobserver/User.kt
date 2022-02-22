package com.dbflow5.contentobserver

import com.dbflow5.annotation.Column
import com.dbflow5.annotation.GranularNotifications
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table

@GranularNotifications
@Table
data class User(
    @PrimaryKey val id: Int = 0,
    @PrimaryKey val name: String = "",
    @Column val age: Int = 0
)
