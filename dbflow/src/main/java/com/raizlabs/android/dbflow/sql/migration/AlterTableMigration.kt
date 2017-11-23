package com.raizlabs.android.dbflow.sql.migration

import android.support.annotation.CallSuper
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.sql.QueryBuilder
import com.raizlabs.android.dbflow.sql.SQLiteType
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper

/**
 * Description: Provides a very nice way to alter a single table quickly and easily.
 */
class AlterTableMigration<T : Any>(
        /**
         * The table to ALTER
         */
        private val table: Class<T>) : BaseMigration() {

    /**
     * The query we use
     */
    private val query by lazy { QueryBuilder().append("ALTER").appendSpaceSeparated("TABLE") }

    /**
     * The query to rename the table with
     */
    private var renameQuery: QueryBuilder? = null

    /**
     * The columns to ALTER within a table
     */
    private val internalColumnDefinitions: MutableList<QueryBuilder> by lazy { mutableListOf<QueryBuilder>() }

    private val columnNames: MutableList<String> by lazy { arrayListOf<String>() }

    /**
     * The old name of the table before renaming it
     */
    private var oldTableName: String? = null

    val alterTableQueryBuilder: QueryBuilder
        get() = query

    override fun migrate(database: DatabaseWrapper) {
        // "ALTER TABLE "
        var sql = alterTableQueryBuilder.query
        val tableName = FlowManager.getTableName(table)

        // "{oldName}  RENAME TO {newName}"
        // Since the structure has been updated already, the manager knows only the new name.
        if (renameQuery != null) {
            val renameQuery = QueryBuilder(sql).appendQuotedIfNeeded(oldTableName)
                    .append(this.renameQuery!!.query)
                    .append(tableName)
                    .toString()
            database.execSQL(renameQuery)
        }

        // We have column definitions to add here
        // ADD COLUMN columnName {type}
        if (internalColumnDefinitions.isNotEmpty()) {
            SQLite.select().from(table).limit(0).query(database)?.use { cursor ->
                sql = QueryBuilder(sql).append(tableName).toString()
                for (i in internalColumnDefinitions.indices) {
                    val columnDefinition = internalColumnDefinitions[i]
                    val columnName = QueryBuilder.stripQuotes(columnNames!![i])
                    if (cursor.getColumnIndex(columnName) == -1) {
                        database.execSQL("$sql ADD COLUMN ${columnDefinition.query}")
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
        renameQuery = QueryBuilder().append(" RENAME").appendSpaceSeparated("TO")
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
        val queryBuilder = QueryBuilder()
                .append(QueryBuilder.quoteIfNeeded(columnName)).appendSpace().appendSQLiteType(sqLiteType)
        internalColumnDefinitions.add(queryBuilder)
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
        val queryBuilder = QueryBuilder()
                .append(QueryBuilder.quoteIfNeeded(columnName)).appendSpace().appendSQLiteType(sqLiteType)
                .appendSpace().append("REFERENCES ").append(referenceClause)
        internalColumnDefinitions.add(queryBuilder)
        columnNames.add(columnName)
    }

    /**
     * @return The query that renames the table.
     */
    fun getRenameQuery(): String {
        val queryBuilder = QueryBuilder(alterTableQueryBuilder.query).appendQuotedIfNeeded(oldTableName)
                .append(renameQuery).append(FlowManager.getTableName(table))
        return queryBuilder.query
    }

    /**
     * @return A List of column definitions that add op to a table in the DB.
     */
    fun getColumnDefinitions(): List<String> {
        val sql = QueryBuilder(alterTableQueryBuilder).append(FlowManager.getTableName(table)).toString()
        return internalColumnDefinitions.map { QueryBuilder(sql).appendSpaceSeparated("ADD COLUMN").append(it.query).query }
    }
}
