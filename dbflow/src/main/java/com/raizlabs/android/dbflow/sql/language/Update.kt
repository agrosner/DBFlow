package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.annotation.ConflictAction
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.sql.Query
import com.raizlabs.android.dbflow.sql.QueryBuilder

/**
 * Description: The SQLite UPDATE query. Will update rows in the DB.
 */
class Update<TModel>
/**
 * Constructs new instace of an UPDATE query with the specified table.
 *
 * @param table The table to use.
 */
internal constructor(val table: Class<TModel>) : Query {

    /**
     * The conflict action to resolve updates.
     */
    private var conflictAction: ConflictAction? = ConflictAction.NONE

    override val query: String
        get() {
            val queryBuilder = QueryBuilder("UPDATE ")
            conflictAction?.let { conflictAction ->
                if (conflictAction != ConflictAction.NONE) {
                    queryBuilder.append("OR").appendSpaceSeparated(conflictAction.name)
                }
            }
            queryBuilder.append(FlowManager.getTableName(table)).appendSpace()
            return queryBuilder.query
        }

    fun conflictAction(conflictAction: ConflictAction) = apply {
        this.conflictAction = conflictAction
    }

    fun or(conflictAction: ConflictAction) = conflictAction(conflictAction)

    /**
     * @return This instance.
     * @see ConflictAction.ROLLBACK
     */
    fun orRollback() = conflictAction(ConflictAction.ROLLBACK)

    /**
     * @return This instance.
     * @see ConflictAction.ABORT
     */
    fun orAbort() = conflictAction(ConflictAction.ABORT)

    /**
     * @return This instance.
     * @see ConflictAction.REPLACE
     */
    fun orReplace() = conflictAction(ConflictAction.REPLACE)

    /**
     * @return This instance.
     * @see ConflictAction.FAIL
     */
    fun orFail() = conflictAction(ConflictAction.FAIL)

    /**
     * @return This instance.
     * @see ConflictAction.IGNORE
     */
    fun orIgnore() = conflictAction(ConflictAction.IGNORE)

    /**
     * Begins a SET piece of the SQL query
     *
     * @param conditions The array of conditions that define this SET statement
     * @return A SET query piece of this statement
     */
    fun set(vararg conditions: SQLOperator): Set<TModel> = Set(this, table).conditions(*conditions)
}
