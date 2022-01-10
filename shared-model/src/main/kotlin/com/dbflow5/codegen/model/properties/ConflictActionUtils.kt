package com.dbflow5.codegen.model.properties

import com.dbflow5.annotation.ConflictAction
import com.dbflow5.annotation.ForeignKeyAction

/**
 * Sanitizes the name for DB usage.
 */
val ConflictAction.dbName
    get() = name.replace(
        "_",
        " "
    )

val ForeignKeyAction.dbName
    get() = name.replace(
        "_",
        " "
    )
