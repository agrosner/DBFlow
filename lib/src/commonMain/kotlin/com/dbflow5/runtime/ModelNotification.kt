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

    data class TableChange<Table : Any>(
        override val adapter: DBRepresentable<Table>,
        override val action: ChangeAction,
    ) : ModelNotification<Table>

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
    }
}
