package com.dbflow5.test

import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table

@Table(createWithDatabase = false, temporary = true)
data class TempModel(@PrimaryKey val id: Int)
