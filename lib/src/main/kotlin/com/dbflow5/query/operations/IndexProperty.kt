package com.dbflow5.query.operations

import com.dbflow5.adapter.SQLObjectAdapter
import com.dbflow5.adapter.makeLazySQLObjectAdapter
import com.dbflow5.query.HasAdapter
import com.dbflow5.query.Index
import com.dbflow5.query.createIndexOn
import com.dbflow5.quoteIfNeeded

interface IndexProperty<Table : Any> :
    HasAdapter<Table, SQLObjectAdapter<Table>> {
    val name: String
    val index: Index<Table>
}

inline fun <reified Table : Any> indexProperty(
    indexName: String,
    unique: Boolean,
    vararg properties: Property<*, Table>
) = indexProperty(
    adapter = makeLazySQLObjectAdapter(Table::class),
    indexName,
    unique,
    *properties,
)

fun <Table : Any> indexProperty(
    adapter: SQLObjectAdapter<Table>,
    indexName: String,
    unique: Boolean,
    vararg properties: Property<*, Table>
): IndexProperty<Table> = IndexPropertyImpl(
    name = indexName.quoteIfNeeded(),
    unique = unique,
    adapter = adapter,
    properties = properties.toList(),
)


internal data class IndexPropertyImpl<Table : Any>(
    override val name: String,
    private val unique: Boolean,
    override val adapter: SQLObjectAdapter<Table>,
    private val properties: List<Property<*, Table>>,
) : IndexProperty<Table> {
    override val index: Index<Table> = adapter.createIndexOn(
        name,
        properties[0],
        *properties.slice(1..properties.lastIndex).toTypedArray(),
    )
}