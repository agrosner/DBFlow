package com.dbflow5.query

import com.dbflow5.appendList
import com.dbflow5.appendQuotedIfNeeded
import com.dbflow5.config.FlowManager
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.database.SQLiteException
import com.dbflow5.dropIndex
import com.dbflow5.query.property.IProperty
import com.dbflow5.sql.Query

/**
 * Description: an INDEX class that enables you to index a specific column from a table. This enables
 * faster retrieval on tables, while increasing the database file size. So enable/disable these as necessary.
 */
class Index<TModel>
/**
 * Creates a new index with the specified name
 *
 * @param indexName The name of this index.
 */
(
    /**
     * @return The name of this index.
     */
    val indexName: String,
    /**
     * @return The table this INDEX belongs to.
     */
    val table: Class<TModel>) : Query {
    private val columns: MutableList<NameAlias> = arrayListOf()
    /**
     * @return true if the index is unique
     */
    var isUnique = false
        private set

    override val query: String
        get() = buildString {
            append("CREATE ")
            append(if (isUnique) "UNIQUE " else "")
            append("INDEX IF NOT EXISTS ")
            appendQuotedIfNeeded(indexName)
            append(" ON ").append(FlowManager.getTableName(table))
            append("(").appendList(columns).append(")")
        }

    /**
     * If true, will append the UNIQUE statement to this trigger.
     *
     * @param unique true if unique. If created again, a [SQLiteException] is thrown.
     * @return This instance.
     */
    fun unique(unique: Boolean) = apply {
        isUnique = unique
    }

    /**
     * The table to execute this Index on.
     *
     * @param table      The table to execute index on.
     * @param properties The properties to create an index for.
     * @return This instance.
     */
    fun on(vararg properties: IProperty<*>) = apply {
        properties.forEach { and(it) }
    }

    /**
     * The table to execute this Index on.
     *
     * @param table   The table to execute index on.
     * @param columns The columns to create an index for.
     * @return This instance.
     */
    fun on(firstAlias: NameAlias, vararg columns: NameAlias) = apply {
        and(firstAlias)
        columns.forEach { and(it) }
    }

    /**
     * Appends a column to this index list.
     *
     * @param property The name of the column. If already exists, this op will not be added
     * @return This instance.
     */
    fun and(property: IProperty<*>) = apply {
        if (!columns.contains(property.nameAlias)) {
            columns.add(property.nameAlias)
        }
    }

    /**
     * Appends a column to this index list.
     *
     * @param columnName The name of the column. If already exists, this op will not be added
     * @return This instance.
     */
    fun and(columnName: NameAlias) = apply {
        if (!columns.contains(columnName)) {
            columns.add(columnName)
        }
    }

    fun enable(databaseWrapper: DatabaseWrapper) {
        if (columns.isEmpty()) {
            throw IllegalStateException("There should be at least one column in this index")
        }
        databaseWrapper.execSQL(query)
    }

    fun disable(databaseWrapper: DatabaseWrapper) {
        dropIndex(databaseWrapper, indexName)
    }
}


inline fun <reified T : Any> indexOn(indexName: String,
                                     vararg property: IProperty<*>)
    = index(indexName, T::class).on(*property)

inline fun <reified T : Any> indexOn(indexName: String, firstNameAlias: NameAlias,
                                     vararg arrayOfNameAlias: NameAlias)
    = index(indexName, T::class).on(firstNameAlias, *arrayOfNameAlias)

