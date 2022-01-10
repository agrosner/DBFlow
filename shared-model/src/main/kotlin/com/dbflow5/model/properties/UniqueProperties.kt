package com.dbflow5.model.properties

import com.dbflow5.annotation.ConflictAction

data class UniqueProperties(
    val unique: Boolean,
    val groups: List<Int>,
    val conflictAction: ConflictAction,
)
