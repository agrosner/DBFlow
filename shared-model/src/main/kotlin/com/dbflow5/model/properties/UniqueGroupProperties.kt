package com.dbflow5.model.properties

import com.dbflow5.annotation.ConflictAction

data class UniqueGroupProperties(
    val number: Int,
    val conflictAction: ConflictAction,
)
