package com.dbflow5.codegen.model.properties

import com.dbflow5.annotation.ConflictAction

/**
 * Description: The field is considered DB Not null on conflict.
 */
data class NotNullProperties(
    val conflictAction: ConflictAction = ConflictAction.FAIL,
)
