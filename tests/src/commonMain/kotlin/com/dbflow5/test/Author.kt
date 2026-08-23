package com.dbflow5.test

import com.dbflow5.annotation.Column
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table

/**
 * Parent used as foreign key reference.
 */
@Table
data class Author(
    @PrimaryKey(autoincrement = true) val id: Int,
    @Column(name = "first_name") val firstName: String,
    @Column(name = "last_name") val lastName: String
)
