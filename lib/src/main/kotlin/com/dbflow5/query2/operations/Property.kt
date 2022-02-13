package com.dbflow5.query2.operations

import com.dbflow5.adapter.AdapterCompanion
import com.dbflow5.adapter.SQLObjectAdapter
import com.dbflow5.adapter.makeLazySQLObjectAdapter
import com.dbflow5.query.NameAlias
import com.dbflow5.query.nameAlias
import com.dbflow5.query2.Aliasable
import com.dbflow5.query2.HasAdapter
import com.dbflow5.sql.Query

interface HasDistinct<ValueType, Table : Any> {
    fun distinct(): DistinctProperty<ValueType, Table>
}

interface WithTable<ValueType, Table : Any> :
    HasAdapter<Table, SQLObjectAdapter<Table>> {
    fun withTable(tableName: String = adapter.name): PropertyStart<ValueType, Table>
}

typealias AnyProperty = Property<*, *>

/**
 * Base interface that properties implement.
 */
interface Property<ValueType, Table : Any> :
    Query,
    PropertyChainable<ValueType>,
    OpStart<ValueType>,
    HasAdapter<Table, SQLObjectAdapter<Table>>,
    Operator<ValueType>

interface PropertyStart<ValueType, Table : Any> :
    Property<ValueType, Table>,
    HasDistinct<ValueType, Table>,
    WithTable<ValueType, Table>,
    Aliasable<AliasedProperty<ValueType, Table>>

interface DistinctProperty<ValueType, Table : Any> :
    Property<ValueType, Table>,
    WithTable<ValueType, Table>,
    Aliasable<AliasedProperty<ValueType, Table>>

interface AliasedProperty<ValueType, Table : Any> :
    Property<ValueType, Table>,
    WithTable<ValueType, Table>

fun <ValueType, Table : Any> AdapterCompanion<Table>.property(
    columnName: String,
    valueConverter: SQLValueConverter<ValueType>
):
    PropertyStart<ValueType, Table> =
    PropertyImpl(
        adapter = makeLazySQLObjectAdapter(table),
        nameAlias = columnName.nameAlias,
        valueConverter = valueConverter,
    )

fun <ValueType, Table : Any> SQLObjectAdapter<Table>.property(
    nameAlias: NameAlias,
    valueConverter: SQLValueConverter<ValueType>
):
    PropertyStart<ValueType, Table> =
    PropertyImpl(
        adapter = this,
        nameAlias = nameAlias,
        valueConverter = valueConverter,
    )

inline fun <reified ValueType, Table : Any> SQLObjectAdapter<Table>.property(
    nameAlias: NameAlias,
): PropertyStart<ValueType, Table> =
    property(
        nameAlias = nameAlias,
        valueConverter = inferValueConverter()
    )

inline fun <reified ValueType, Table : Any> AdapterCompanion<Table>.property(
    columnName: String,
): PropertyStart<ValueType, Table> {
    return property(
        columnName = columnName,
        valueConverter = inferValueConverter(),
    )
}

internal data class PropertyImpl<ValueType, Table : Any>(
    override val adapter: SQLObjectAdapter<Table>,
    override val nameAlias: NameAlias,
    override val valueConverter: SQLValueConverter<ValueType>,
    private val distinct: Boolean = false,
) : PropertyStart<ValueType, Table>,
    DistinctProperty<ValueType, Table>,
    AliasedProperty<ValueType, Table> {

    /**
     * Its query is just the property name.
     */
    override val query: String = nameAlias.fullQuery
    override fun distinct(): DistinctProperty<ValueType, Table> =
        copy(
            distinct = true,
        )

    override fun withTable(tableName: String): PropertyStart<ValueType, Table> =
        copy(
            nameAlias = nameAlias.newBuilder()
                .withTable(tableName)
                .build(),
        )

    override fun `as`(
        name: String,
        shouldAddIdentifierToAlias: Boolean
    ): AliasedProperty<ValueType, Table> =
        copy(
            nameAlias = nameAlias.newBuilder()
                .shouldAddIdentifierToAliasName(shouldAddIdentifierToAlias)
                .`as`(name)
                .build(),
        )
}