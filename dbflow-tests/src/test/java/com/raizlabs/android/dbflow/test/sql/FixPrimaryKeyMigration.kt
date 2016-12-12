package com.raizlabs.android.dbflow.test.sql

import android.database.Cursor

import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.config.FlowLog
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.sql.QueryBuilder
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.sql.migration.BaseMigration
import com.raizlabs.android.dbflow.sql.queriable.StringQuery
import com.raizlabs.android.dbflow.structure.Model
import com.raizlabs.android.dbflow.structure.ModelAdapter
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper

/**
 * Description: Example of fixing a bug where Primary Key was treated as a [PrimaryKey.rowID],
 * not a [PrimaryKey.autoincrement].
 */
abstract class FixPrimaryKeyMigration<TableClass> : BaseMigration() {

    override fun migrate(database: DatabaseWrapper) {
        val tableSchema = selectTableQuery.query(database)

        if (tableSchema != null && tableSchema.moveToFirst()) {
            val creationQuery = tableSchema.getString(0)
            // we run query if it was incorrectly categorized this way
            if (validateCreationQuery(creationQuery)) {
                /// create table
                database.execSQL(tempCreationQuery)

                val insertQuery = insertTransferQuery
                database.execSQL(insertQuery)

                database.execSQL(String.format("DROP TABLE %1s", FlowManager.getTableName(tableClass)))

                database.execSQL(String.format("ALTER TABLE %1s RENAME to %1s", QueryBuilder.quote(tempTableName),
                        FlowManager.getTableName(tableClass)))
            } else {
                FlowLog.log(FlowLog.Level.I, String.format("Creation Query %1s is already in correct format.", creationQuery))
            }

        }

        tableSchema?.close()
    }

    internal val selectTableQuery: StringQuery<TableClass>
        get() = StringQuery(tableClass,
                String.format("SELECT sql FROM sqlite_master WHERE name='%1s'", tableName))

    internal val tempCreationQuery: String
        get() {
            val adapter = FlowManager.getModelAdapter(tableClass)
            var adapterCreationQuery = adapter.creationQuery
            adapterCreationQuery = adapterCreationQuery.replace(tableName, tempTableName)
            return adapterCreationQuery
        }

    private val tempTableName: String
        get() = tableName + "_temp"

    internal fun validateCreationQuery(query: String): Boolean {
        return query.startsWith(
                String.format("CREATE TABLE %1s(%1s INTEGER,", QueryBuilder.quote(tableName),
                        QueryBuilder.quote(FlowManager.getModelAdapter(tableClass).autoIncrementingColumnName)))
    }

    internal val tableName: String
        get() = QueryBuilder.stripQuotes(FlowManager.getTableName(tableClass))

    internal val insertTransferQuery: String
        get() {
            var query = SQLite.insert(tableClass)
                    .asColumns()
                    .select(SQLite
                            .select(*FlowManager.getModelAdapter(tableClass)
                                    .allColumnProperties).from(tableClass)).query
            query = query.replaceFirst(tableName.toRegex(), tempTableName)
            return query
        }

    abstract val tableClass: Class<TableClass>
}
