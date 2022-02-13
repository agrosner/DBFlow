package com.dbflow5.query2.operations

import com.dbflow5.adapter.SQLObjectAdapter
import com.dbflow5.query.NameAlias
import com.dbflow5.sql.Query

/**
 * Represents a table name when used.
 */
interface TableNameLiteral<Table : Any> :
    BaseOperator.SingleValueOperator<String>,
    OpStart<String>,
    Query

fun <Table : Any> SQLObjectAdapter<Table>.tableNameLiteral(): TableNameLiteral<Table> =
    TableNameLiteralImpl(
        sqlObjectAdapter = this,
    )

internal data class TableNameLiteralImpl<Table : Any>(
    private val sqlObjectAdapter: SQLObjectAdapter<Table>,
    override val valueConverter: SQLValueConverter<String> = inferValueConverter(),
    override val nameAlias: NameAlias = NameAlias.builder(sqlObjectAdapter.name).build(),
) : TableNameLiteral<Table> {
    override val query: String by lazy { sqlObjectAdapter.name }

    override val value: String = sqlObjectAdapter.name

    override fun `as`(
        name: String,
        shouldAddIdentifierToAlias: Boolean
    ): Operator<String> =
        copy(
            nameAlias = nameAlias.newBuilder()
                .shouldAddIdentifierToAliasName(shouldAddIdentifierToAlias)
                .`as`(name)
                .build(),
        )
}