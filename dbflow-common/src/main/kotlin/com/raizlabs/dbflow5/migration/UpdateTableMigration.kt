package com.raizlabs.dbflow5.migration

import kotlin.reflect.KClass
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.query.BaseQueriable
import com.raizlabs.dbflow5.query.OperatorGroup
import com.raizlabs.dbflow5.query.SQLOperator
import com.raizlabs.dbflow5.query.update

/**
 * Description: Provides a simple way to update a table's field or fields quickly in a migration.
 * It ties an SQLite [com.raizlabs.android.dbflow.sql.language.Update]
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
    private val table: KClass<T>) : BaseMigration() {

    /**
     * Builds the conditions for the WHERE part of our query
     */
    private val whereOperatorGroup: OperatorGroup by lazy { OperatorGroup.nonGroupingClause() }

    /**
     * The conditions to use to set fields in the update query
     */
    private val setOperatorGroup: OperatorGroup by lazy { OperatorGroup.nonGroupingClause() }

    val updateStatement: BaseQueriable<T>
        get() = update(table)
            .set(setOperatorGroup)
            .where(whereOperatorGroup)

    /**
     * This will append a condition to this migration. It will execute each of these in succession with the order
     * that this is called.
     *
     * @param conditions The conditions to append
     */
    fun set(vararg conditions: SQLOperator) = apply {
        setOperatorGroup.andAll(*conditions)
    }

    fun where(vararg conditions: SQLOperator) = apply {
        whereOperatorGroup.andAll(*conditions)
    }

    override fun migrate(database: DatabaseWrapper) {
        updateStatement.execute(database)
    }
}
