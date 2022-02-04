package com.dbflow5.migration

import androidx.annotation.CallSuper
import com.dbflow5.adapter.SQLObjectAdapter
import com.dbflow5.appendQuotedIfNeeded
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.query.select
import com.dbflow5.quoteIfNeeded
import com.dbflow5.sql.SQLiteType
import com.dbflow5.stripQuotes

/**
 * Description: Provides a very nice way to alter a single table quickly and easily.
 */
open class AlterTableMigration<T : Any>(
    /**
     * The table adapter to ALTER
     */
    adapterGetter: () -> SQLObjectAdapter<T>
) : BaseMigration() {

    protected val adapter by lazy(adapterGetter)

    /**
     * The query to rename the table with
     */
    private var renameQuery: String? = null

    /**
     * The columns to ALTER within a table
     */
    private val internalColumnDefinitions: MutableList<String> by lazy { mutableListOf() }

    private val columnNames: MutableList<String> by lazy { mutableListOf() }

    /**
     * The old name of the table before renaming it
     */
    private var oldTableName: String? = null

    override fun migrate(database: DatabaseWrapper) {
        // "ALTER TABLE "
        var sql = ALTER_TABLE
        val tableName = adapter.name

        // "{oldName}  RENAME TO {newName}"
        // Since the structure has been updated already, the manager knows only the new name.
        renameQuery?.let { renameQuery ->
            database.execSQL(buildString {
                append(sql)
                appendQuotedIfNeeded(oldTableName)
                append(renameQuery)
                append(tableName)
            })
        }

        // We have column definitions to add here
        // ADD COLUMN columnName {type}
        if (internalColumnDefinitions.isNotEmpty()) {
            (select from adapter limit 0).cursor(database)?.use { cursor ->
                sql = "$sql$tableName"
                for (i in internalColumnDefinitions.indices) {
                    val columnDefinition = internalColumnDefinitions[i]
                    val columnName = columnNames[i].stripQuotes()
                    if (cursor.getColumnIndex(columnName) == -1) {
                        database.execSQL("$sql ADD COLUMN $columnDefinition")
                    }
                }
            }
        }
    }

    @CallSuper
    override fun onPostMigrate() {
        // cleanup and make fields eligible for garbage collection
        renameQuery = null
        internalColumnDefinitions.clear()
        columnNames.clear()
    }

    /**
     * Call this to rename a table to a new name.
     *
     * @param oldName The new name to call the table.
     * @return This instance
     */
    fun renameFrom(oldName: String) = apply {
        oldTableName = oldName
        renameQuery = " RENAME TO "
    }

    /**
     * Add a column to the DB. This does not necessarily need to be reflected in the [T],
     * but it is recommended.
     *
     * @param sqLiteType The type of column represented in the DB.
     * @param columnName The name of the column to add. Use the "_Table" class for the specified table.
     * @param defaultValue the default value as a String representation.
     * Leaving parameter as null leaves the case out.
     * For NULL column add defaultValue = "NULL". Encapsulate the value in quotes "\'name\'" if it's a string.
     * @return This instance
     */
    @JvmOverloads
    fun addColumn(sqLiteType: SQLiteType, columnName: String, defaultValue: String? = null) =
        apply {
            var addStatement = "${columnName.quoteIfNeeded()} ${sqLiteType.name}"
            if (defaultValue != null) {
                addStatement += " DEFAULT $defaultValue"
            }
            internalColumnDefinitions.add(addStatement)
            columnNames.add(columnName)
        }

    /**
     * Add a column to the DB. This does not necessarily need to be reflected in the [T],
     * but it is recommended.
     *
     * @param sqLiteType      The type of column that pertains to an [SQLiteType]
     * @param columnName      The name of the column to add. Use the "$Table" class for the specified table.
     * @param referenceClause The clause of the references that this foreign key points to.
     * @return This instance
     */
    fun addForeignKeyColumn(sqLiteType: SQLiteType, columnName: String, referenceClause: String) =
        apply {
            internalColumnDefinitions.add("${columnName.quoteIfNeeded()} ${sqLiteType.name} REFERENCES $referenceClause")
            columnNames.add(columnName)
        }

    /**
     * @return The query that renames the table.
     */
    fun getRenameQuery(): String = buildString {
        append(ALTER_TABLE)
        appendQuotedIfNeeded(oldTableName)
        append(renameQuery)
        append(adapter.name)
    }

    /**
     * @return A List of column definitions that add op to a table in the DB.
     */
    fun getColumnDefinitions(): List<String> {
        val sql = "$ALTER_TABLE ${adapter.name}"
        return internalColumnDefinitions.map { "$sql ADD COLUMN $it" }
    }

    companion object {

        const val ALTER_TABLE = "ALTER TABLE "
    }
}
