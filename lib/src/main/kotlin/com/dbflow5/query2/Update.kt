package com.dbflow5.query2

import com.dbflow5.adapter.RetrievalAdapter
import com.dbflow5.adapter.SQLObjectAdapter
import com.dbflow5.annotation.ConflictAction
import com.dbflow5.query.OperatorGroup
import com.dbflow5.query.SQLOperator
import com.dbflow5.sql.Query

interface Update<Table : Any> : Query,
    HasAdapter<Table, SQLObjectAdapter<Table>>,
    HasAssociatedAdapters

interface UpdateWithConflict<Table : Any> :
    Update<Table>,
    HasConflictAction,
    Conflictable<UpdateWithConflict<Table>>,
    Settable<Table>, Indexable<Table>

interface UpdateWithSet<Table : Any> :
    Update<Table>,
    HasConflictAction,
    HasOperatorGroup,
    Whereable<Table, Long, UpdateStart<Table>>,
    Indexable<Table>, ExecutableQuery<Long> {
    infix fun and(condition: SQLOperator): UpdateWithSet<Table>
}

interface UpdateStart<Table : Any> :
    Update<Table>,
    Conflictable<UpdateWithConflict<Table>>,
    Whereable<Table, Long, UpdateStart<Table>>,
    Settable<Table>, HasOperatorGroup,
    Indexable<Table>,
    ExecutableQuery<Long>

fun <Table : Any> SQLObjectAdapter<Table>.update(): UpdateStart<Table> = UpdateImpl(
    adapter = this,
)

internal data class UpdateImpl<Table : Any>(
    override val conflictAction: ConflictAction = ConflictAction.NONE,
    override val adapter: SQLObjectAdapter<Table>,
    override val operatorGroup: OperatorGroup = OperatorGroup.nonGroupingClause()
        .setAllCommaSeparated(true),
    override val resultFactory: ResultFactory<Long> = UpdateDeleteResultFactory,
) : UpdateStart<Table>, UpdateWithConflict<Table>,
    UpdateWithSet<Table> {

    override val associatedAdapters: List<RetrievalAdapter<*>> = listOf(adapter)

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
            operatorGroup = operatorGroup.andAll(*conditions),
        )

    override fun set(condition: SQLOperator): UpdateWithSet<Table> =
        copy(
            operatorGroup = operatorGroup.and(condition),
        )

    override fun and(condition: SQLOperator): UpdateWithSet<Table> =
        copy(
            operatorGroup = operatorGroup.and(condition),
        )
}