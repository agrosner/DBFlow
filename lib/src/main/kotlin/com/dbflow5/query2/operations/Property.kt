package com.dbflow5.query2.operations

import com.dbflow5.adapter.AdapterCompanion
import com.dbflow5.adapter.SQLObjectAdapter
import com.dbflow5.adapter.makeLazySQLObjectAdapter
import com.dbflow5.query.NameAlias
import com.dbflow5.query.nameAlias
import com.dbflow5.query2.HasAdapter
import com.dbflow5.sql.Query

interface HasDistinct<ValueType, Table : Any> {
    fun distinct(): DistinctProperty<ValueType, Table>
}

interface WithTable<ValueType, Table : Any> {
    fun withTable(): Property<ValueType, Table>
}

interface Property<ValueType, Table : Any> :
    Query,
    OpStart<ValueType>,
    HasAdapter<Table, SQLObjectAdapter<Table>>

interface PropertyStart<ValueType, Table : Any> :
    Property<ValueType, Table>,
    HasDistinct<ValueType, Table>,
    WithTable<ValueType, Table>

interface DistinctProperty<ValueType, Table : Any> :
    PropertyStart<ValueType, Table>,
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

inline fun <reified ValueType, Table : Any> AdapterCompanion<Table>.property(
    columnName: String,
): PropertyStart<ValueType, Table> {
    return property(
        columnName = columnName,
        valueConverter = inferValueConverter(),
    )
}

/**
 * Description:
 */
internal data class PropertyImpl<ValueType, Table : Any>(
    override val adapter: SQLObjectAdapter<Table>,
    override val nameAlias: NameAlias,
    override val valueConverter: SQLValueConverter<ValueType>,
    private val distinct: Boolean = false,
) : PropertyStart<ValueType, Table>,
    DistinctProperty<ValueType, Table> {

    /**
     * Its query is just the property name.
     */
    override val query: String = nameAlias.query
    override fun distinct(): DistinctProperty<ValueType, Table> =
        copy(
            distinct = true,
        )

    override fun withTable(): Property<ValueType, Table> =
        copy(
            nameAlias = nameAlias.newBuilder()
                .withTable(adapter.name)
                .build(),
        )
}