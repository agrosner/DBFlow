package com.dbflow5.runtime

import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.config.FlowManager
import com.dbflow5.query2.operations.BaseOperator
import com.dbflow5.structure.ChangeAction
import kotlin.reflect.KClass

/**
 * Description:
 */
sealed interface ModelNotification<Table : Any> {
    val action: ChangeAction
    val adapter: ModelAdapter<Table>
    val table: KClass<Table>

    data class TableChange<Table : Any>(
        override val table: KClass<Table>,
        override val action: ChangeAction,
    ) : ModelNotification<Table> {
        override val adapter: ModelAdapter<Table> by lazy {
            FlowManager.getModelAdapter(table)
        }
    }

    data class ModelChange<Table : Any>(
        val changedFields: List<BaseOperator.SingleValueOperator<*>>,
        override val action: ChangeAction,
        override val adapter: ModelAdapter<Table>,
    ) : ModelNotification<Table> {
        override val table: KClass<Table> = adapter.table

        constructor(
            model: Table,
            action: ChangeAction,
            adapter: ModelAdapter<Table>
        ) : this(
            adapter.getPrimaryConditionClause(model)
                .filterIsInstance<BaseOperator.SingleValueOperator<*>>(),
            action, adapter
        )
    }
}
