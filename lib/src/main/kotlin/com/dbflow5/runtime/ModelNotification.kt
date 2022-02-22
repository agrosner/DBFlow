package com.dbflow5.runtime

import com.dbflow5.adapter2.ModelAdapter
import com.dbflow5.query.operations.BaseOperator
import com.dbflow5.structure.ChangeAction
import kotlin.reflect.KClass

/**
 * Description:
 */
sealed interface ModelNotification<Table : Any> {
    val action: ChangeAction
    val table: KClass<Table>

    data class TableChange<Table : Any>(
        override val table: KClass<Table>,
        override val action: ChangeAction,
    ) : ModelNotification<Table>

    data class ModelChange<Table : Any>(
        val changedFields: List<BaseOperator.SingleValueOperator<*>>,
        override val action: ChangeAction,
        override val table: KClass<Table>,
    ) : ModelNotification<Table> {

        constructor(
            model: Table,
            action: ChangeAction,
            adapter: ModelAdapter<Table>
        ) : this(
            adapter.getPrimaryModelClause(model),
            action, adapter.table
        )
    }
}
