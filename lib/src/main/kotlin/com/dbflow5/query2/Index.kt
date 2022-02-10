package com.dbflow5.query2

import com.dbflow5.adapter.SQLObjectAdapter
import com.dbflow5.appendQuotedIfNeeded
import com.dbflow5.database.SQLiteException
import com.dbflow5.query.NameAlias
import com.dbflow5.query.property.IProperty
import com.dbflow5.query2.operations.Property
import com.dbflow5.sql.Query

/**
 * Description: an INDEX class that enables you to index a specific column from a table. This enables
 * faster retrieval on tables, while increasing the database file size.
 * So enable/disable these as necessary.
 */
interface Index<Table : Any> : Query,
    HasAdapter<Table, SQLObjectAdapter<Table>>,
    QueryExecutableIgnoreResult

interface UniqueIndex<Table : Any> : Index<Table>

interface IndexStart<Table : Any> : Index<Table> {
    /**
     * Make this index unique, appending a UNIQUE.
     * A [SQLiteException] will get thrown for same type of index.
     */
    fun unique(): UniqueIndex<Table>
}

/**
 * Creates an index with [name] based on the [SQLObjectAdapter] used.
 *
 * set [ifNotExists] to false, if you wish for [SQLiteException] to get thrown on recreation.
 */
fun <Table : Any> SQLObjectAdapter<Table>.createIndexOn(
    name: String,
    property: Property<*, Table>,
    vararg restProperties: Property<*, Table>,
    ifNotExists: Boolean = true,
): IndexStart<Table> = IndexImpl(
    adapter = this,
    name = name,
    columns = mutableListOf(property).apply {
        addAll(restProperties)
    }.map { it.nameAlias },
    ifNotExists = ifNotExists,
)

/**
 * Creates an index with [name] based on the [SQLObjectAdapter] used.
 *
 * set [ifNotExists] to false, if you wish for [SQLiteException] to get thrown on recreation.
 */
@Deprecated(message = "remove")
fun <Table : Any> SQLObjectAdapter<Table>.createIndexOn(
    name: String,
    property: IProperty<*>,
    vararg restProperties: IProperty<*>,
    ifNotExists: Boolean = true,
): IndexStart<Table> = IndexImpl(
    adapter = this,
    name = name,
    columns = mutableListOf(property).apply {
        addAll(restProperties)
    }.map { it.nameAlias },
    ifNotExists = ifNotExists,
)

internal data class IndexImpl<Table : Any>(
    override val adapter: SQLObjectAdapter<Table>,
    private val columns: List<NameAlias> = listOf(),
    private val name: String,
    private val unique: Boolean = false,
    private val ifNotExists: Boolean,
) : IndexStart<Table>, UniqueIndex<Table> {
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
}
