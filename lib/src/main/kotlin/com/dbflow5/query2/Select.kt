package com.dbflow5.query2

import com.dbflow5.adapter.RetrievalAdapter
import com.dbflow5.adapter.SQLObjectAdapter
import com.dbflow5.query.ModelQueriable
import com.dbflow5.query.NameAlias
import com.dbflow5.query.enclosedQuery
import com.dbflow5.query.property.IProperty
import com.dbflow5.query.property.Property
import com.dbflow5.sql.Query

sealed class QualifierType(val value: String) {
    object Distinct : QualifierType("DISTINCT")
    object All : QualifierType("ALL")
    object None : QualifierType("NONE")
}

interface SelectWithAlias<Table : Any> : Query,
    Joinable<Table>,
    HasAssociatedAdapters {
    val tableAlias: NameAlias
}

interface SelectWithQualifier<Table : Any> : Query,
    Aliasable<SelectWithAlias<Table>>,
    Joinable<Table>,
    HasAssociatedAdapters {
    val qualifier: QualifierType
}

interface SelectWithModelQueriable<Table : Any> : Query,
    HasAssociatedAdapters {
    val modelQueriable: ModelQueriable<Table>?
}

interface SelectWithJoins<Table : Any> : Query,
    HasAssociatedAdapters, Joinable<Table> {
    val joins: List<Join<*, *>>
}

interface Select<Table : Any> : Query,
    HasAdapter<Table, SQLObjectAdapter<Table>>,
    Aliasable<SelectWithAlias<Table>>,
    Joinable<Table>,
    HasAssociatedAdapters {

    val properties: List<IProperty<*>>

    fun distinct(): SelectWithQualifier<Table>

    infix fun from(modelQueriable: ModelQueriable<Table>): SelectWithModelQueriable<Table>
}

fun <Table : Any> SQLObjectAdapter<Table>.select(): Select<Table> = SelectImpl(
    adapter = this,
    properties = listOf(Property.ALL_PROPERTY),
)

fun <Table : Any> SQLObjectAdapter<Table>.select(
    vararg properties: IProperty<*>
): Select<Table> =
    SelectImpl(
        adapter = this,
        properties = properties.toList(),
    )

internal data class SelectImpl<Table : Any>(
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
) : Select<Table>, SelectWithQualifier<Table>,
    SelectWithAlias<Table>,
    SelectWithModelQueriable<Table>,
    SelectWithJoins<Table> {
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

    override fun distinct(): SelectWithQualifier<Table> =
        copy(
            qualifier = QualifierType.Distinct,
        )

    override fun `as`(name: String): SelectWithAlias<Table> =
        copy(
            tableAlias = tableAlias.newBuilder()
                .`as`(name)
                .build()
        )

    override fun from(modelQueriable: ModelQueriable<Table>): SelectWithModelQueriable<Table> =
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