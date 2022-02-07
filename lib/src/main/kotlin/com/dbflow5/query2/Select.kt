package com.dbflow5.query2

import com.dbflow5.adapter.RetrievalAdapter
import com.dbflow5.adapter.SQLObjectAdapter
import com.dbflow5.config.FlowManager
import com.dbflow5.query.ModelQueriable
import com.dbflow5.query.NameAlias
import com.dbflow5.query.count
import com.dbflow5.query.enclosedQuery
import com.dbflow5.query.property.IProperty
import com.dbflow5.query.property.Property
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
        properties = listOf(Property.ALL_PROPERTY),
        resultFactory = SelectResultFactory(this),
    )

fun <Table : Any> SQLObjectAdapter<Table>.select(
    vararg properties: IProperty<*>
): SelectStart<Table, SelectResult<Table>> =
    SelectImpl(
        adapter = this,
        properties = properties.toList(),
        resultFactory = SelectResultFactory(this),
    )

/**
 * Provides a [Select] that returns a long result based on
 * the count of rows.
 */
fun <Table : Any> SQLObjectAdapter<Table>.selectCountOf(
    vararg properties: IProperty<*>,
): SelectStart<Table, CountResultFactory.Count> =
    SelectImpl(
        adapter = this,
        properties = listOf(count(*properties)),
        resultFactory = CountResultFactory,
    )

/**
 * A select providing a custom result factory return type.
 */
fun <Table : Any, Result> SQLObjectAdapter<Table>.select(
    resultFactory: ResultFactory<Result>,
    vararg properties: IProperty<*>,
): SelectStart<Table, Result> =
    SelectImpl(
        adapter = this,
        properties = properties.toList(),
        resultFactory = resultFactory
    )

internal data class SelectImpl<Table : Any, Result>(
    private val qualifier: QualifierType = QualifierType.None,
    private val properties: List<IProperty<*>> = emptyList(),
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
            append("${properties.joinToString()} FROM ${
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

    override fun `as`(name: String): SelectWithAlias<Table, Result> =
        copy(
            tableAlias = tableAlias.newBuilder()
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
        modelQueriable: ModelQueriable<JoinTable>,
        joinType: JoinType
    ): Join<Table, JoinTable, Result> = JoinImpl(
        this,
        type = joinType,
        adapter = (modelQueriable.adapter as? SQLObjectAdapter<JoinTable>)
            ?: throw IllegalStateException(
                "To use a Join, " +
                    "you must provide a valid DB adapter " +
                    "type (View or Table)."
            ),
        modelQueriable = modelQueriable,
    )

    override val associatedAdapters: List<RetrievalAdapter<*>> =
        linkedSetOf<RetrievalAdapter<*>>(adapter)
            .apply {
                joins.mapTo(this) { it.adapter }
            }
            .toList()
}
