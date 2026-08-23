package com.dbflow5.test

import com.dbflow5.annotation.GranularNotifications
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table

@GranularNotifications
@Table
data class SimpleModel(@PrimaryKey val name: String)
