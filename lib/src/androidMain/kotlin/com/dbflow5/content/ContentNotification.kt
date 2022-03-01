package com.dbflow5.content

import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.query.operations.BaseOperator
import com.dbflow5.structure.ChangeAction

/**
 * Represents a notification used in [ContentResolverNotifier]
 */
sealed interface ContentNotification {
    val tableName: String
    val action: ChangeAction
    val authority: String

    fun mutate(
        tableName: String = this.tableName,
        action: ChangeAction = this.action,
        authority: String = this.authority,
    ): ContentNotification

    data class TableChange(
        override val action: ChangeAction,
        override val authority: String,
        override val tableName: String,
    ) : ContentNotification {
        override fun mutate(
            tableName: String,
            action: ChangeAction,
            authority: String
        ): ContentNotification = copy(
            tableName = tableName,
            action = action,
            authority = authority,
        )
    }

    data class ModelChange<Table : Any>(
        override val tableName: String,
        override val action: ChangeAction,
        override val authority: String,
        val changedFields: List<BaseOperator.SingleValueOperator<*>>,
    ) : ContentNotification {
        constructor(
            model: Table,
            adapter: ModelAdapter<Table>,
            action: ChangeAction,
            authority: String,
        ) : this(
            tableName = adapter.name,
            action = action,
            authority = authority,
            changedFields = adapter.getPrimaryModelClause(
                model
            )
                // TODO: we should enforce this at operator group level.
                .filterIsInstance<BaseOperator.SingleValueOperator<Table>>()
        )

        /**
         * Removes changed fields, since those don't exist in common interface.
         */
        override fun mutate(
            tableName: String,
            action: ChangeAction,
            authority: String,
        ): ContentNotification = copy(
            tableName = tableName,
            action = action,
            authority = authority,
            changedFields = listOf(),
        )
    }
}
