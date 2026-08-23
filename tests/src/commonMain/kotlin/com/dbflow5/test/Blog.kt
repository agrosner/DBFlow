package com.dbflow5.test

import com.dbflow5.annotation.ForeignKey
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table

/**
 * Example of simple foreign key object with one foreign key object.
 */
@Table
data class Blog(
    @PrimaryKey(autoincrement = true) val id: Int,
    val name: String,
    @ForeignKey(saveForeignKeyModel = true) val author: Author?,
)
