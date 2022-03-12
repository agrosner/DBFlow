package com.dbflow5.query.migration

import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.database.migration.Migration
import com.dbflow5.database.scope.MigrationScope
import com.dbflow5.query.ExecutableQuery
import com.dbflow5.quoteIfNeeded
import com.dbflow5.sql.Query
import com.dbflow5.sql.SQLiteType

interface AlterTable : Query

sealed interface ColumnAlter : Query {
    val name: String
    val type: SQLiteType
    val defaultValue: String?
    override val query: String
        get() = "ADD COLUMN ${name.quoteIfNeeded()} ${type.name} DEFAULT $defaultValue"

    data class Plain(
        override val name: String,
        override val type: SQLiteType,
        override val defaultValue: String? = null,
    ) : ColumnAlter

    data class ForeignKey(
        internal val referencesClause: String,
        override val name: String,
        override val type: SQLiteType,
        // new foreign keys must have a default value of null when foreign constraints enabled.
        override val defaultValue: String? = "NULL",
    ) : ColumnAlter {
        override val query: String by lazy {
            buildString {
                append("${super.query} REFERENCES $referencesClause")
            }
        }
    }
}

/**
 * Begins an ALTER TABLE statement used in a [Migration]
 */
@Suppress("unused")
fun MigrationScope.alterTable(name: String): AlterTableStart =
    AlterTableStart(oldTableName = name)

data class AlterTableStart internal constructor(
    private val oldTableName: String,
) {

    infix fun addColumn(columnAlter: ColumnAlter): AlterTableEnd = AlterTableImpl(
        oldTableName = oldTableName,
        type = AlterTableType.AddColumn(columnAlter),
    )

    infix fun dropColumn(name: String): AlterTableEnd = AlterTableImpl(
        oldTableName = oldTableName,
        type = AlterTableType.DropColumn(name),
    )

    infix fun renameTo(newName: String): AlterTableEnd = AlterTableImpl(
        oldTableName = oldTableName,
        type = AlterTableType.TableRename(newName),
    )

    fun renameColumn(oldName: String, to: String): AlterTableEnd = AlterTableImpl(
        oldTableName = oldTableName,
        type = AlterTableType.ColumnRename(oldName, to)
    )
}

interface AlterTableEnd : AlterTable,
    ExecutableQuery<Unit> {
    val type: AlterTableType
}

sealed interface AlterTableType : Query {
    data class AddColumn(
        private val columnAlter: ColumnAlter,
    ) : AlterTableType {
        override val query: String = columnAlter.query
    }

    data class DropColumn(
        private val columnName: String,
    ) : AlterTableType {
        override val query: String = "DROP COLUMN ${columnName.quoteIfNeeded()}"
    }

    data class ColumnRename(
        private val oldColumnName: String,
        private val newColumnName: String,
    ) : AlterTableType {
        override val query: String =
            "RENAME COLUMN ${oldColumnName.quoteIfNeeded()} TO ${newColumnName.quoteIfNeeded()}"
    }

    data class TableRename(
        private val newTableName: String
    ) : AlterTableType {
        override val query: String = "RENAME TO $newTableName"
    }
}

internal data class AlterTableImpl(
    private val oldTableName: String,
    override val type: AlterTableType,
) : AlterTable,
    AlterTableEnd {
    override val query: String = "ALTER TABLE ${oldTableName.quoteIfNeeded()} ${type.query}"

    override suspend fun execute(db: DatabaseWrapper) {
        db.execSQL(query)
    }
}
