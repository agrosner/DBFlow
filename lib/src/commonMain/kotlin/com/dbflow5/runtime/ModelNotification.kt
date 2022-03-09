package com.dbflow5.runtime

import com.dbflow5.adapter.DBRepresentable
import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.query.operations.BaseOperator
import com.dbflow5.structure.ChangeAction

/**
 * Description:
 */
sealed interface ModelNotification<Table : Any> {
    val action: ChangeAction
    val adapter: DBRepresentable<Table>

    fun print(): String

    data class TableChange<Table : Any>(
        override val adapter: DBRepresentable<Table>,
        override val action: ChangeAction,
    ) : ModelNotification<Table> {
        override fun print(): String {
            return "TableChange (${action.name}): ${adapter.name}"
        }
    }

    data class ModelChange<Table : Any>(
        val changedFields: List<BaseOperator.SingleValueOperator<*>>,
        override val action: ChangeAction,
        override val adapter: DBRepresentable<Table>,
    ) : ModelNotification<Table> {

        constructor(
            model: Table,
            action: ChangeAction,
            adapter: ModelAdapter<Table>
        ) : this(
            adapter.getPrimaryModelClause(model),
            action, adapter,
        )

        override fun print(): String {
            return "ModelChange (${action.name}): ${adapter.name} changedFields: ${
                changedFields.joinToString {
                    it.query
                }
            }"
        }
    }
}
