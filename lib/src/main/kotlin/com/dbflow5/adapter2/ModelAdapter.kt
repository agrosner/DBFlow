package com.dbflow5.adapter2

import com.dbflow5.annotation.opts.InternalDBFlowApi
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.query.hasData
import com.dbflow5.query.operations.OperatorGroup
import com.dbflow5.query.operations.OperatorGrouping
import com.dbflow5.query.operations.Property
import com.dbflow5.query.selectCountOf
import com.dbflow5.sql.Query
import kotlin.reflect.KClass

typealias PropertyGetter<Table> = (columnName: String) -> Property<*, Table>

/**
 * Used by generated code.
 */
inline fun <reified Table : Any> modelAdapter(
    name: String,
    creationSQL: CompilableQuery,
    ops: TableOps<Table>,
    createWithDatabase: Boolean,
    primaryModelClauseGetter: PrimaryModelClauseGetter<Table>,
    noinline propertyGetter: PropertyGetter<Table>,
) =
    ModelAdapter(
        table = Table::class,
        ops = ops,
        propertyGetter = propertyGetter,
        name = name,
        creationSQL = creationSQL,
        createWithDatabase = createWithDatabase,
        primaryModelClauseGetter = primaryModelClauseGetter,
    )

/**
 * Main table usage object. Retrieve instance of class via generated db scope methods.
 */
data class ModelAdapter<Table : Any>
@InternalDBFlowApi
constructor(
    val table: KClass<Table>,
    private val ops: TableOps<Table>,
    private val propertyGetter: PropertyGetter<Table>,
    override val name: String,
    override val creationSQL: CompilableQuery,
    override val createWithDatabase: Boolean,
    private val primaryModelClauseGetter: PrimaryModelClauseGetter<Table>,
) : TableOps<Table> by ops, DBRepresentable<Table> {
    override val dropSQL: CompilableQuery = CompilableQuery(
        "DROP TABLE IF EXISTS $name"
    )

    override val type: KClass<Table> = table

    fun getProperty(columnName: String) = propertyGetter(columnName)

    fun getPrimaryModelClause(model: Table) = primaryModelClauseGetter.get(model)

    suspend fun DatabaseWrapper.exists(model: Table) =
        selectCountOf()
            .where(getPrimaryModelClause(model)
                .fold(OperatorGroup.nonGroupingClause()) { acc: OperatorGrouping<Query>, operator ->
                    acc and operator
                })
            .hasData(this)
}