package com.dbflow5.ksp.model

import com.dbflow5.annotation.ConflictAction

data class UniqueGroupModel(
    val number: Int,
    val conflictAction: ConflictAction,
    val fields: List<FieldModel>,
)
