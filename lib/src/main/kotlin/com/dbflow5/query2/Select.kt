package com.dbflow5.query2

import com.dbflow5.adapter.RetrievalAdapter
import com.dbflow5.adapter.SQLObjectAdapter
import com.dbflow5.query.ModelQueriable
import com.dbflow5.query.NameAlias
import com.dbflow5.query.count
import com.dbflow5.query.enclosedQuery
import com.dbflow5.query.property.IProperty
import com.dbflow5.query.property.Property

sealed class QualifierType(val value: String) {
    object Distinct : QualifierType("DISTINCT")
    object All : QualifierType("ALL")
    object None : QualifierType("NONE")
}

/**
 * All selects implement this
 */
interface Select<Table : Any, Result> : ExecutableQuery<Result>

interface SelectWithAlias<Table : Any, Result> :
    Select<Table, Result>,
    Joinable<Table>,
    HasAssociatedAdapters,
    Indexable<Table>,
    Whereable<Table, Result, Select<Table, Result>,
        SQLObjectAdapter<Table>> {
    val tableAlias: NameAlias
}

interface SelectWithQualifier<Table : Any, Result> :
    Select<Table, Result>,
    Aliasable<SelectWithAlias<Table, Result>>,
    Joinable<Table>,
    HasAssociatedAdapters,
    Indexable<Table>,
    Whereable<Table, Result, Select<Table, Result>,
        SQLObjectAdapter<Table>> {
    val qualifier: QualifierType
}

interface SelectWithModelQueriable<Table : Any, Result> :
    Select<Table, Result>,
    HasAssociatedAdapters,
    Joinable<Table>,
    Indexable<Table>,
    Whereable<Table, Result, Select<Table, Result>,
        SQLObjectAdapter<Table>> {
    val modelQueriable: ModelQueriable<Table>?
}

interface SelectWithJoins<Table : Any, Result> :
    Select<Table, Result>,
    HasAssociatedAdapters, Joinable<Table>,
    Indexable<Table>,
    Whereable<Table, Result, Select<Table, Result>,
        SQLObjectAdapter<Table>> {
    val joins: List<Join<*, *>>
}

interface SelectStart<Table : Any, Result> :
    Select<Table, Result>,
    HasAdapter<Table, SQLObjectAdapter<Table>>,
    Aliasable<SelectWithAlias<Table, Result>>,
    Joinable<Table>,
    HasAssociatedAdapters,
    Indexable<Table>,
    Whereable<Table,
        Result,
        Select<Table, Result>,
        SQLObjectAdapter<Table>> {

    val properties: List<IProperty<*>>

    fun distinct(): SelectWithQualifier<Table, Result>

    infix fun from(modelQueriable: ModelQueriable<Table>): SelectWithModelQueriable<Table, Result>
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
): SelectStart<Table, Long> =
    SelectImpl(
        adapter = this,
        properties = listOf(count(*properties)),
        resultFactory = LongForQueryResultFactory,
    )

internal data class SelectImpl<Table : Any, Result>(
    override val qualifier: QualifierType = QualifierType.None,
    override val properties: List<IProperty<*>> = emptyList(),
    override val adapter: SQLObjectAdapter<Table>,
    override val tableAlias: NameAlias = NameAlias.Builder(
        adapter.name
    ).build(),
    /**
     * Subquery instead of [tableAlias]
     */
    override val modelQueriable: ModelQueriable<Table>? = null,
    override val joins: List<Join<*, *>> = listOf(),
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
                modelQueriable?.let { append(it.enclosedQuery) }
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

    override fun from(modelQueriable: ModelQueriable<Table>): SelectWithModelQueriable<Table, Result> =
        copy(
            modelQueriable = modelQueriable,
        )

    override fun <JoinTable : Any> join(
        adapter: SQLObjectAdapter<JoinTable>,
        joinType: JoinType
    ): Join<Table, JoinTable> = JoinImpl(
        this,
        type = joinType,
        adapter = adapter,
    )

    override fun <JoinTable : Any> join(
        modelQueriable: ModelQueriable<JoinTable>,
        joinType: JoinType
    ): Join<Table, JoinTable> = JoinImpl(
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
