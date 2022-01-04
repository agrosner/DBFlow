package com.dbflow5.processor.definition.behavior

import com.dbflow5.annotation.ForeignKeyAction
import com.dbflow5.processor.definition.column.ColumnDefinition

/**
 * Defines how Primary Key columns behave. If has autoincrementing column or ROWID, the [associatedColumn] is not null.
 */
data class PrimaryKeyColumnBehavior(
    val hasRowID: Boolean,
    /**
     * Either [hasRowID] or [hasAutoIncrement] or null.
     */
    val associatedColumn: ColumnDefinition?,
    val hasAutoIncrement: Boolean)


/**
 * Defines how Foreign Key columns behave.
 */
data class ForeignKeyColumnBehavior(
    val onDelete: ForeignKeyAction,
    val onUpdate: ForeignKeyAction,
    val saveForeignKeyModel: Boolean,
    val deferred: Boolean
)