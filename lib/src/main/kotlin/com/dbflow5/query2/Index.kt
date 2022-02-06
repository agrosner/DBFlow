package com.dbflow5.query2

import com.dbflow5.adapter.SQLObjectAdapter
import com.dbflow5.appendQuotedIfNeeded
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.dropIndex
import com.dbflow5.query.NameAlias
import com.dbflow5.query.property.IProperty
import com.dbflow5.sql.Query

interface UniqueIndex<Table : Any> : Query,
    HasAdapter<Table, SQLObjectAdapter<Table>> {

    val unique: Boolean
}

/**
 * Description:
 */
interface Index<Table : Any> : Query,
    HasAdapter<Table, SQLObjectAdapter<Table>>,
    QueryExecutableIgnoreResult {

    fun unique(): UniqueIndex<Table>

}

fun <Table : Any> SQLObjectAdapter<Table>.createIndexOn(
    name: String,
    property: IProperty<*>,
    vararg restProperties: IProperty<*>,
    ifNotExists: Boolean = true,
): Index<Table> = IndexImpl(
    adapter = this,
    name = name,
    columns = mutableListOf(property).apply {
        addAll(restProperties)
    }.map { it.nameAlias },
    ifNotExists = ifNotExists,
)

internal data class IndexImpl<Table : Any>(
    override val adapter: SQLObjectAdapter<Table>,
    val columns: List<NameAlias> = listOf(),
    val name: String,
    override val unique: Boolean = false,
    val ifNotExists: Boolean,
) : Index<Table>, UniqueIndex<Table> {
    override val query: String by lazy {
        buildString {
            append("CREATE ")
            if (unique) append("UNIQUE ")
            append("INDEX ")
            if (ifNotExists) append("IF NOT EXISTS ")
            appendQuotedIfNeeded(name)
            append(" ON ${adapter.name}(${columns.joinToString()})")
        }
    }

    override fun unique(): UniqueIndex<Table> =
        copy(
            unique = true,
        )

    fun drop(databaseWrapper: DatabaseWrapper) {
        dropIndex(databaseWrapper, name)
    }
}