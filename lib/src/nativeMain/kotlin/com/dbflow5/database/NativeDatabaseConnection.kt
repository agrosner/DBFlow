package com.dbflow5.database

import co.touchlab.sqliter.DatabaseConnection
import co.touchlab.sqliter.getVersion
import co.touchlab.sqliter.interop.SQLiteExceptionErrorCode
import com.dbflow5.config.FlowLog
import kotlinx.atomicfu.atomic

class NativeDatabaseConnection(
    override val generatedDatabase: GeneratedDatabase,
    internal val db: DatabaseConnection,
) : com.dbflow5.database.DatabaseConnection {

    private var inTransaction by atomic(false)

    override val isInTransaction: Boolean
        get() = inTransaction
    override val version: Int
        get() = db.getVersion()

    override fun execute(query: String) = rethrowDBFlowException {
        FlowLog.log(FlowLog.Level.D, "Executing query", query)
        db.rawExecSql(query)
    }

    override suspend fun <R> executeTransaction(dbFn: suspend com.dbflow5.database.DatabaseConnection.() -> R): R {
        // only allow a single transaction to occur.
        val wasInTransaction = inTransaction
        try {
            if (!wasInTransaction) {
                db.beginTransaction()
                inTransaction = true
            }
            val result = dbFn()
            if (!wasInTransaction) {
                db.setTransactionSuccessful()
            }
            return result
        } finally {
            if (!wasInTransaction) {
                db.endTransaction()
                inTransaction = false
            }
        }
    }

    override fun compileStatement(rawQuery: String): DatabaseStatement = rethrowDBFlowException {
        // library throws exception if trying to create statement of table not created
        NativeDatabaseStatement(db.createStatement(rawQuery))
    }

    override fun rawQuery(query: String): FlowCursor = rethrowDBFlowException {
        // library throws exception if trying to create statement of table not created
        NativeFlowCursor(db.createStatement(query).query())
    }

    override val isOpen: Boolean
        get() = !db.closed
}

fun co.touchlab.sqliter.interop.SQLiteException.toDBFlowSQLiteException() =
    SQLiteException("A Database Error Occurred", this)

inline fun <T> rethrowDBFlowException(fn: () -> T) = try {
    fn()
} catch (e: co.touchlab.sqliter.interop.SQLiteException) {
    throw e.toDBFlowSQLiteException()
} catch (e: SQLiteExceptionErrorCode) {
    throw e.toDBFlowSQLiteException()
}
