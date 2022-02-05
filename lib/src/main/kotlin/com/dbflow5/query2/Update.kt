package com.dbflow5.query2

import com.dbflow5.adapter.SQLObjectAdapter
import com.dbflow5.annotation.ConflictAction
import com.dbflow5.query.OperatorGroup
import com.dbflow5.query.SQLOperator
import com.dbflow5.sql.Query

interface UpdateWithConflict<Table : Any> : HasConflictAction,
    Conflictable<UpdateWithConflict<Table>>, Query,
    Settable<Table>

interface UpdateWithSet<Table : Any> : HasConflictAction,
    Query, HasOperatorGroup, Whereable<Table, UpdateWithSet<Table>>

interface Update<Table : Any> : Query,
    Conflictable<UpdateWithConflict<Table>>,
    HasAdapter<Table, SQLObjectAdapter<Table>>,
    Settable<Table>, HasOperatorGroup

fun <Table : Any> SQLObjectAdapter<Table>.update(): Update<Table> = UpdateImpl(
    adapter = this,
)

internal data class UpdateImpl<Table : Any>(
    override val conflictAction: ConflictAction = ConflictAction.NONE,
    override val adapter: SQLObjectAdapter<Table>,
    override val operatorGroup: OperatorGroup = OperatorGroup.nonGroupingClause()
        .setAllCommaSeparated(true),
) : Update<Table>, UpdateWithConflict<Table>,
    UpdateWithSet<Table>, Whereable<Table, UpdateWithSet<Table>> {

    override val query: String by lazy {
        buildString {
            append("UPDATE ")
            if (conflictAction != ConflictAction.NONE) {
                append("OR ${conflictAction.name} ")
            }
            append("${adapter.name} ")
            if (operatorGroup.isNotEmpty()) {
                append("SET ${operatorGroup.query}")
            }
        }
    }

    override fun or(action: ConflictAction): UpdateWithConflict<Table> = copy(
        conflictAction = action,
    )

    override fun set(vararg conditions: SQLOperator): UpdateWithSet<Table> =
        copy(
            operatorGroup = operatorGroup.andAll(*conditions)
        )

    override fun set(condition: SQLOperator): UpdateWithSet<Table> =
        copy(
            operatorGroup = operatorGroup.and(condition)
        )

    override fun where(operator: SQLOperator): Where<Table, UpdateWithSet<Table>> =
        adapter.where(operator)
}