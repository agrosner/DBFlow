package com.raizlabs.android.dbflow.sql.language

import android.database.sqlite.SQLiteDoneException
import com.raizlabs.android.dbflow.config.FlowLog
import com.raizlabs.android.dbflow.runtime.NotifyDistributor
import com.raizlabs.android.dbflow.sql.longForQuery
import com.raizlabs.android.dbflow.sql.queriable.Queriable
import com.raizlabs.android.dbflow.structure.ChangeAction
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement
import com.raizlabs.android.dbflow.structure.database.DatabaseStatementWrapper
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper
import com.raizlabs.android.dbflow.structure.database.FlowCursor

/**
 * Description: Base implementation of something that can be queried from the database.
 */
abstract class BaseQueriable<TModel : Any> protected constructor(
        private val databaseWrapper: DatabaseWrapper,
        /**
         * @return The table associated with this INSERT
         */
        val table: Class<TModel>) : Queriable, Actionable {

    abstract override val primaryAction: ChangeAction

    override fun longValue(): Long {
        try {
            val query = query
            FlowLog.log(FlowLog.Level.V, "Executing query: " + query)
            return longForQuery(databaseWrapper, query)
        } catch (sde: SQLiteDoneException) {
            // catch exception here, log it but return 0;
            FlowLog.logWarning(sde)
        }

        return 0
    }

    override fun hasData(): Boolean = longValue() > 0

    override fun query(): FlowCursor? {
        if (primaryAction == ChangeAction.INSERT) {
            // inserting, let's compile and insert
            val databaseStatement = compileStatement()
            databaseStatement.executeInsert()
            databaseStatement.close()
        } else {
            val query = query
            FlowLog.log(FlowLog.Level.V, "Executing query: " + query)
            databaseWrapper.execSQL(query)
        }
        return null
    }

    override fun executeInsert(): Long =
            compileStatement().executeInsert()

    override fun execute() {
        val cursor = query()
        if (cursor != null) {
            cursor.close()
        } else {
            // we dont query, we're executing something here.
            NotifyDistributor.get().notifyTableChanged(table, primaryAction)
        }
    }

    override fun compileStatement(): DatabaseStatement {
        val query = query
        FlowLog.log(FlowLog.Level.V, "Compiling Query Into Statement: " + query)
        return DatabaseStatementWrapper(databaseWrapper.compileStatement(query), this)
    }

    override fun toString(): String = query
}
