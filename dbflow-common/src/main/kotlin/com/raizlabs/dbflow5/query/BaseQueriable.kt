package com.raizlabs.dbflow5.query

import kotlin.reflect.KClass
import com.raizlabs.dbflow5.config.FlowLog
import com.raizlabs.dbflow5.database.*
import com.raizlabs.dbflow5.longForQuery
import com.raizlabs.dbflow5.runtime.NotifyDistributor
import com.raizlabs.dbflow5.structure.ChangeAction
import com.raizlabs.dbflow5.use

/**
 * Description: Base implementation of something that can be queried from the database.
 */
abstract class BaseQueriable<TModel : Any> protected constructor(
    /**
     * @return The table associated with this INSERT
     */
    val table: KClass<TModel>) : Queriable, Actionable {

    abstract override val primaryAction: ChangeAction

    override fun longValue(databaseWrapper: DatabaseWrapper): Long {
        try {
            val query = query
            FlowLog.log(FlowLog.Level.V, "Executing query: $query")
            return longForQuery(databaseWrapper, query)
        } catch (sde: SQLiteException) {
            // catch exception here, log it but return 0;
            FlowLog.logWarning(sde)
        }

        return 0
    }

    override fun hasData(databaseWrapper: DatabaseWrapper): Boolean = longValue(databaseWrapper) > 0

    override fun cursor(databaseWrapper: DatabaseWrapper): FlowCursor? {
        if (primaryAction == ChangeAction.INSERT) {
            // inserting, let's compile and insert
            compileStatement(databaseWrapper).use { it.executeInsert() }
        } else {
            val query = query
            FlowLog.log(FlowLog.Level.V, "Executing query: " + query)
            databaseWrapper.execSQL(query)
        }
        return null
    }

    override fun executeInsert(databaseWrapper: DatabaseWrapper): Long =
        compileStatement(databaseWrapper).use { it.executeInsert() }

    override fun execute(databaseWrapper: DatabaseWrapper) {
        val cursor = cursor(databaseWrapper)
        if (cursor != null) {
            cursor.close()
        } else {
            // we dont query, we're executing something here.
            NotifyDistributor.get().notifyTableChanged(table, primaryAction)
        }
    }

    override fun compileStatement(databaseWrapper: DatabaseWrapper): DatabaseStatement {
        val query = query
        FlowLog.log(FlowLog.Level.V, "Compiling Query Into Statement: " + query)
        return DatabaseStatementWrapper(databaseWrapper.compileStatement(query), this)
    }

    override fun toString(): String = query
}
