package com.dbflow5.adapter

import com.dbflow5.annotation.opts.InternalDBFlowApi
import com.dbflow5.database.DatabaseConnection
import com.dbflow5.query.hasData
import com.dbflow5.query.operations.BaseOperator
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
    ModelAdapterImpl(
        table = Table::class,
        ops = ops,
        propertyGetter = propertyGetter,
        tableSqlName = name,
        creationSQL = creationSQL,
        createWithDatabase = createWithDatabase,
        primaryModelClauseGetter = primaryModelClauseGetter,
    )

/**
 * Main table adapter. Generated table companions implement this by extending
 * [ModelAdapterImpl] (registered as `User.Companion as ModelAdapter<User>`).
 */
interface ModelAdapter<Table : Any> : WritableDBRepresentable<Table>, TableOps<Table> {

    fun getPrimaryModelClause(model: Table): List<BaseOperator.SingleValueOperator<*>>

    suspend fun DatabaseConnection.exists(model: Table): Boolean
}

/**
 * Default [ModelAdapter] implementation used by generated adapter factories.
 */
open class ModelAdapterImpl<Table : Any>
@InternalDBFlowApi
constructor(
    override val table: KClass<Table>,
    private val ops: TableOps<Table>,
    private val propertyGetter: PropertyGetter<Table>,
    private val tableSqlName: String,
    override val creationSQL: CompilableQuery,
    override val createWithDatabase: Boolean,
    private val primaryModelClauseGetter: PrimaryModelClauseGetter<Table>,
) : ModelAdapter<Table>, TableOps<Table> by ops, AdapterCompanion<Table> {
    override val name: String get() = tableSqlName

    override fun sqlName(): String = tableSqlName

    override val dropSQL: CompilableQuery = CompilableQuery(
        "DROP TABLE IF EXISTS $tableSqlName"
    )

    override val type: KClass<Table> = table

    override fun getProperty(columnName: String) = propertyGetter(columnName)

    override fun getPrimaryModelClause(model: Table) = primaryModelClauseGetter.get(model)

    override suspend fun DatabaseConnection.exists(model: Table) =
        selectCountOf()
            .where(getPrimaryModelClause(model)
                .fold(OperatorGroup.nonGroupingClause()) { acc: OperatorGrouping<Query>, operator ->
                    acc and operator
                })
            .hasData(this)
}
