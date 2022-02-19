package com.dbflow5.sqlcipher

import com.dbflow5.annotation.Column
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table

@Table
class CipherModel(
    @PrimaryKey(autoincrement = true) var id: Long = 0,
    @Column var name: String? = null
)