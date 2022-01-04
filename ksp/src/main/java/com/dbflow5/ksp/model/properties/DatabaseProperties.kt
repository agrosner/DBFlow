package com.dbflow5.ksp.model.properties

import com.dbflow5.annotation.ConflictAction
import com.squareup.kotlinpoet.ClassName

data class DatabaseProperties(
    val version: Int,
    val foreignKeyConstraintsEnforced: Boolean,
    val insertConflict: ConflictAction,
    val updateConflict: ConflictAction,
    val tables: List<ClassName>,
    val views: List<ClassName>,
    val queries: List<ClassName>,
    val migrations: List<ClassName>,
)