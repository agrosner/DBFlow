package com.dbflow5.test

import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table

@Table(createWithDatabase = false)
data class DontCreateModel(@PrimaryKey val id: Int)
