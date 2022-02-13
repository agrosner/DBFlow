package com.dbflow5.query2

import com.dbflow5.adapter.RetrievalAdapter
import com.dbflow5.adapter.SQLObjectAdapter
import com.dbflow5.config.FlowManager
import com.dbflow5.query.NameAlias
import com.dbflow5.query.enclosedQuery
import com.dbflow5.query2.operations.AnyOperator
import com.dbflow5.query2.operations.Property
import com.dbflow5.query2.operations.StandardMethods
import com.dbflow5.query2.operations.invoke
import com.dbflow5.query2.operations.literalOf
import kotlin.reflect.KClass

sealed class QualifierType(val value: String) {
    object Distinct : QualifierType("DISTINCT")
    object All : QualifierType("ALL")
    object None : QualifierType("NONE")
}

/**
 * All selects implement this
 */
interface Select<Table : Any, Result> :
    ExecutableQuery<Result>,
    HasAdapter<Table, SQLObjectAdapter<Table>>,
    HasAssociatedAdapters,
    Whereable<Table, Result, Select<Table, Result>>

/**
 * Used to provide "select from table" notation as an alternative
 * to adapter.select() method.
 */
object SelectToken {
    /**
     * Provided for compatibility reasons.
     */
    infix fun <Table : Any> from(adapter: SQLObjectAdapter<Table>): SelectStart<Table, SelectResult<Table>> =
        adapter.select()

    infix fun <Table : Any> from(table: KClass<Table>): SelectStart<Table, SelectResult<Table>> =
        FlowManager.getSQLObjectAdapter(table).select()
}

/**
 * Existing select-compatible function.
 * Provides "select from table" method over adapter.select() method.
 */
val select = SelectToken

interface SelectWithAlias<Table : Any, Result> :
    Select<Table, Result>,
    Joinable<Table, Result>,
    Indexable<Table>

interface SelectWithQualifier<Table : Any, Result> :
    Select<Table, Result>,
    Aliasable<SelectWithAlias<Table, Result>>,
    Joinable<Table, Result>,
    Indexable<Table>

interface SelectWithModelQueriable<Table : Any, Result> :
    Select<Table, Result>,
    Joinable<Table, Result>,
    Indexable<Table>

interface SelectWithJoins<Table : Any, Result> :
    Select<Table, Result>,
    Joinable<Table, Result>,
    Indexable<Table>

interface SelectStart<Table : Any, Result> :
    Select<Table, Result>,
    Joinable<Table, Result>,
    Indexable<Table>,
    Aliasable<SelectWithAlias<Table, Result>> {

    fun distinct(): SelectWithQualifier<Table, Result>

    infix fun from(modelQueriable: ExecutableQuery<Result>): SelectWithModelQueriable<Table, Result>
}

fun <Table : Any> SQLObjectAdapter<Table>.select(): SelectStart<Table, SelectResult<Table>> =
    SelectImpl(
        adapter = this,
        properties = listOf(literalOf("*")),
        resultFactory = SelectResultFactory(this),
    )

fun <Table : Any> SQLObjectAdapter<Table>.select(
    vararg properties: Property<*, Table>
): SelectStart<Table, SelectResult<Table>> =
    select(
        resultFactory = SelectResultFactory(this),
        properties = properties
    )

/**
 * Select from [SQLObjectAdapter] widened to allow non-table
 * properties.
 */
fun <Table : Any> SQLObjectAdapter<Table>.select(
    vararg operators: AnyOperator,
): SelectStart<Table, SelectResult<Table>> =
    select(
        resultFactory = SelectResultFactory(this),
        operators = operators,
    )

/**
 * Provides a [Select] that returns a long result based on
 * the count of rows.
 */
fun <Table : Any> SQLObjectAdapter<Table>.selectCountOf(
    vararg properties: Property<*, Table>,
): SelectStart<Table, CountResultFactory.Count> =
    SelectImpl(
        adapter = this,
        properties = listOf(StandardMethods.Count(*properties)),
        resultFactory = CountResultFactory,
    )

/**
 * Provides a [Select] that returns a long result based on
 * the count of rows. Widens to allow any operator
 */
fun <Table : Any> SQLObjectAdapter<Table>.selectCountOf(
    operator: AnyOperator,
    vararg operators: AnyOperator,
): SelectStart<Table, CountResultFactory.Count> =
    SelectImpl(
        adapter = this,
        properties = listOf(
            StandardMethods.Count(
                *mutableListOf(operator)
                    .apply { addAll(operators) }.toTypedArray()
            )
        ),
        resultFactory = CountResultFactory,
    )

/**
 * A select providing a custom result factory return type.
 */
fun <Table : Any, Result> SQLObjectAdapter<Table>.select(
    resultFactory: ResultFactory<Result>,
    vararg properties: Property<*, Table>,
): SelectStart<Table, Result> =
    SelectImpl(
        adapter = this,
        properties = properties.toList(),
        resultFactory = resultFactory
    )

/**
 * A select providing a custom result factory return type, widened
 * to allow any operation type.
 */
fun <Table : Any, Result> SQLObjectAdapter<Table>.select(
    resultFactory: ResultFactory<Result>,
    vararg operators: AnyOperator,
): SelectStart<Table, Result> =
    SelectImpl(
        adapter = this,
        properties = operators.toList(),
        resultFactory = resultFactory
    )

internal data class SelectImpl<Table : Any, Result>(
    private val qualifier: QualifierType = QualifierType.None,
    private val properties: List<AnyOperator> = emptyList(),
    override val adapter: SQLObjectAdapter<Table>,
    private val tableAlias: NameAlias = NameAlias.Builder(
        adapter.name
    ).build(),
    /**
     * Subquery instead of [tableAlias]
     */
    private val subquery: ExecutableQuery<Result>? = null,
    val joins: List<Join<*, *, Result>> = listOf(),
    override val resultFactory: ResultFactory<Result>,
) : Select<Table, Result>,
    SelectStart<Table, Result>,
    SelectWithQualifier<Table, Result>,
    SelectWithAlias<Table, Result>,
    SelectWithModelQueriable<Table, Result>,
    SelectWithJoins<Table, Result> {
    override val query: String by lazy {
        buildString {
            append("SELECT ")
            append(
                when (qualifier) {
                    QualifierType.Distinct -> "DISTINCT "
                    QualifierType.All -> "ALL "
                    QualifierType.None -> ""
                }
            )
            append("${properties.joinToString { it.query }} FROM ${
                subquery?.let { append(it.enclosedQuery) }
                    ?: tableAlias
            }")
            if (joins.isNotEmpty()) {
                append(" ${joins.joinToString(separator = "") { it.query }}")
            } else {
                append(" ")
            }
        }
    }

    override fun distinct(): SelectWithQualifier<Table, Result> =
        copy(
            qualifier = QualifierType.Distinct,
        )

    override fun `as`(
        name: String,
        shouldAddIdentifierToAlias: Boolean
    ): SelectWithAlias<Table, Result> =
        copy(
            tableAlias = tableAlias.newBuilder()
                .shouldAddIdentifierToAliasName(shouldAddIdentifierToAlias)
                .`as`(name)
                .build()
        )

    override fun from(modelQueriable: ExecutableQuery<Result>): SelectWithModelQueriable<Table, Result> =
        copy(
            subquery = modelQueriable,
        )

    override fun <JoinTable : Any> join(
        adapter: SQLObjectAdapter<JoinTable>,
        joinType: JoinType
    ): Join<Table, JoinTable, Result> = JoinImpl(
        this,
        type = joinType,
        adapter = adapter,
    )

    override fun <JoinTable : Any> join(
        hasAdapter: HasAdapter<JoinTable, SQLObjectAdapter<JoinTable>>,
        joinType: JoinType
    ): Join<Table, JoinTable, Result> = JoinImpl(
        this,
        type = joinType,
        adapter = hasAdapter.adapter,
        queryNameAlias = literalOf(hasAdapter).nameAlias,
    )

    override val associatedAdapters: List<RetrievalAdapter<*>> =
        linkedSetOf<RetrievalAdapter<*>>(adapter)
            .apply {
                joins.mapTo(this) { it.adapter }
            }
            .toList()
}
