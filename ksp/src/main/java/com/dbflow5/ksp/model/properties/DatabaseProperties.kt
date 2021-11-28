package com.dbflow5.ksp.model.properties

import com.dbflow5.annotation.ConflictAction

data class DatabaseProperties(
    val version: Int,
    val foreignKeyConstraintsEnforced: Boolean,
    val insertConflict: ConflictAction,
    val updateConflict: ConflictAction,
)