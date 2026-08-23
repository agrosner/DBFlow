package com.dbflow5.test

import com.dbflow5.annotation.ConflictAction
import com.dbflow5.annotation.PrimaryKey
import com.dbflow5.annotation.Table

@Table(
    updateConflict = ConflictAction.FAIL,
    insertConflict = ConflictAction.FAIL
)
data class NumberModel(@PrimaryKey val id: Int)
