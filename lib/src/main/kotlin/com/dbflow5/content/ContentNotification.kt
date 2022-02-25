package com.dbflow5.content

import com.dbflow5.adapter.DBRepresentable
import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.query.operations.BaseOperator
import com.dbflow5.structure.ChangeAction

/**
 * Represents a notification used in [ContentResolverNotifier]
 */
sealed interface ContentNotification<Table : Any> {
    val dbRepresentable: DBRepresentable<Table>
    val action: ChangeAction
    val authority: String

    fun mutate(
        dbRepresentable: DBRepresentable<Table> = this.dbRepresentable,
        action: ChangeAction = this.action,
        authority: String = this.authority,
    ): ContentNotification<Table>

    data class TableChange<Table : Any>(
        override val action: ChangeAction,
        override val authority: String,
        override val dbRepresentable: DBRepresentable<Table>,
    ) : ContentNotification<Table> {
        override fun mutate(
            dbRepresentable: DBRepresentable<Table>,
            action: ChangeAction,
            authority: String
        ): ContentNotification<Table> = copy(
            dbRepresentable = dbRepresentable,
            action = action,
            authority = authority,
        )
    }

    data class ModelChange<Table : Any>(
        override val dbRepresentable: DBRepresentable<Table>,
        override val action: ChangeAction,
        override val authority: String,
        val changedFields: List<BaseOperator.SingleValueOperator<*>>,
    ) : ContentNotification<Table> {
        constructor(
            model: Table,
            adapter: ModelAdapter<Table>,
            action: ChangeAction,
            authority: String,
        ) : this(
            dbRepresentable = adapter,
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
            dbRepresentable: DBRepresentable<Table>,
            action: ChangeAction,
            authority: String,
        ): ContentNotification<Table> = copy(
            dbRepresentable = dbRepresentable,
            action = action,
            authority = authority,
            changedFields = listOf(),
        )
    }
}
