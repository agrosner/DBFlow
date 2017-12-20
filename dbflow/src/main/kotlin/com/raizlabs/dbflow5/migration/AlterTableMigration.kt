package com.raizlabs.dbflow5.migration

import android.support.annotation.CallSuper
import com.raizlabs.dbflow5.appendQuotedIfNeeded
import com.raizlabs.dbflow5.config.FlowManager
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.query.select
import com.raizlabs.dbflow5.quoteIfNeeded
import com.raizlabs.dbflow5.sql.SQLiteType
import com.raizlabs.dbflow5.stripQuotes

/**
 * Description: Provides a very nice way to alter a single table quickly and easily.
 */
class AlterTableMigration<T : Any>(
        /**
         * The table to ALTER
         */
        private val table: Class<T>) : BaseMigration() {

    /**
     * The query to rename the table with
     */
    private var renameQuery: String? = null

    /**
     * The columns to ALTER within a table
     */
    private val internalColumnDefinitions: MutableList<String> by lazy { mutableListOf<String>() }

    private val columnNames: MutableList<String> by lazy { arrayListOf<String>() }

    /**
     * The old name of the table before renaming it
     */
    private var oldTableName: String? = null

    override fun migrate(database: DatabaseWrapper) {
        // "ALTER TABLE "
        var sql = ALTER_TABLE
        val tableName = FlowManager.getTableName(table)

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
            (select from table limit 0).cursor(database)?.use { cursor ->
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
     * Call this to rename a table to a new name, such as changing either the [com.raizlabs.android.dbflow.structure.Model] class name
     * or by changing the name through a [com.raizlabs.android.dbflow.annotation.Table]
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
     * @return This instance
     */
    fun addColumn(sqLiteType: SQLiteType, columnName: String) = apply {
        internalColumnDefinitions.add("${columnName.quoteIfNeeded()} ${sqLiteType.name}")
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
    fun addForeignKeyColumn(sqLiteType: SQLiteType, columnName: String, referenceClause: String) = apply {
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
        append(FlowManager.getTableName(table))
    }

    /**
     * @return A List of column definitions that add op to a table in the DB.
     */
    fun getColumnDefinitions(): List<String> {
        val sql = "$ALTER_TABLE ${FlowManager.getTableName(table)}"
        return internalColumnDefinitions.map { "$sql ADD COLUMN $it" }
    }

    companion object {

        const val ALTER_TABLE = "ALTER TABLE "
    }
}
