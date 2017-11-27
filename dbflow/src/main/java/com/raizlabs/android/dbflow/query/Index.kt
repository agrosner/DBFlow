package com.raizlabs.android.dbflow.query

import com.raizlabs.android.dbflow.appendList
import com.raizlabs.android.dbflow.appendQuotedIfNeeded
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.sql.Query
import com.raizlabs.android.dbflow.dropIndex
import com.raizlabs.android.dbflow.query.property.IProperty
import com.raizlabs.android.dbflow.database.DatabaseWrapper

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
        private val databaseWrapper: DatabaseWrapper,
        /**
         * @return The name of this index.
         */
        val indexName: String) : Query {

    /**
     * @return The table this INDEX belongs to.
     */
    var table: Class<TModel>? = null
        private set
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
            append(" ON ").append(FlowManager.getTableName(table!!))
            append("(").appendList(columns).append(")")
        }

    /**
     * If true, will append the UNIQUE statement to this trigger.
     *
     * @param unique true if unique. If created again, a [android.database.SQLException] is thrown.
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
    fun on(table: Class<TModel>, vararg properties: IProperty<*>) = apply {
        this.table = table
        properties.forEach { and(it) }
    }

    /**
     * The table to execute this Index on.
     *
     * @param table   The table to execute index on.
     * @param columns The columns to create an index for.
     * @return This instance.
     */
    fun on(table: Class<TModel>, firstAlias: NameAlias, vararg columns: NameAlias) = apply {
        this.table = table
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

    fun enable() {
        if (table == null) {
            throw IllegalStateException("Please call on() to set a table to use this index on.")
        } else if (columns.isEmpty()) {
            throw IllegalStateException("There should be at least one column in this index")
        }
        databaseWrapper.execSQL(query)
    }

    fun disable() {
        dropIndex(databaseWrapper, indexName)
    }
}


inline fun <reified T : Any> DatabaseWrapper.indexOn(indexName: String,
                                                     vararg property: IProperty<*>)
        = index<T>(indexName).on(T::class.java, *property)

inline fun <reified T : Any> DatabaseWrapper.indexOn(indexName: String, firstNameAlias: NameAlias,
                                                     vararg arrayOfNameAlias: NameAlias)
        = index<T>(indexName).on(T::class.java, firstNameAlias, *arrayOfNameAlias)

