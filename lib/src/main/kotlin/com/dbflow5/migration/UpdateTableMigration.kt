package com.dbflow5.migration

import com.dbflow5.adapter.SQLObjectAdapter
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.query2.ExecutableQuery
import com.dbflow5.query2.operations.AnyOperator
import com.dbflow5.query2.operations.Operation
import com.dbflow5.query2.operations.OperatorGroup
import com.dbflow5.query2.update
import kotlinx.coroutines.runBlocking

/**
 * Description: Provides a simple way to update a table's field or fields quickly in a migration.
 * It ties an SQLite [com.dbflow5.sql.language.Update]
 * to migrations whenever we want to batch update tables in a structured manner.
 */
open class UpdateTableMigration<T : Any>
/**
 * Creates an update migration.
 *
 * @param table The table to update
 */
    (
    /**
     * The table to update
     */
    adapterGetter: () -> SQLObjectAdapter<T>
) : BaseMigration() {

    protected val adapter by lazy(adapterGetter)

    /**
     * Builds the conditions for the WHERE part of our query
     */
    private val whereOperatorGroup: OperatorGroup by lazy { OperatorGroup.nonGroupingClause() }

    /**
     * The conditions to use to set fields in the update query
     */
    private val setOperatorGroup: OperatorGroup by lazy { OperatorGroup.nonGroupingClause() }

    private val updateStatement: ExecutableQuery<Long>
        get() = adapter.update()
            .set(setOperatorGroup)
            .where(whereOperatorGroup)

    /**
     * This will append a condition to this migration. It will execute each of these in succession with the order
     * that this is called.
     *
     * @param conditions The conditions to append
     */
    fun set(vararg conditions: AnyOperator) = apply {
        setOperatorGroup.chain(Operation.Comma, *conditions)
    }

    fun where(vararg conditions: AnyOperator) = apply {
        whereOperatorGroup.chain(Operation.Comma, *conditions)
    }

    override fun migrate(database: DatabaseWrapper) {
        runBlocking { updateStatement.execute(database) }
    }
}
