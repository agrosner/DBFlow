package com.raizlabs.dbflow5.database

import com.raizlabs.dbflow5.config.FlowLog
import com.raizlabs.dbflow5.transaction.ITransaction

/**
 * Description: Provides a base implementation that wraps a database, so other databaseForTable engines potentially can
 * be used.
 */
interface DatabaseWrapper {

    val version: Int

    fun execSQL(query: String)

    fun beginTransaction()

    fun setTransactionSuccessful()

    fun endTransaction()

    fun compileStatement(rawQuery: String): DatabaseStatement

    fun compileStatement(rawQuery: String, selectionArgs: Array<String>?): DatabaseStatement {
        return compileStatement(rawQuery).apply { bindAllArgsAsStrings(selectionArgs) }
    }

    fun rawQuery(query: String, selectionArgs: Array<String>?): FlowCursor

    fun query(tableName: String, columns: Array<String>?, selection: String?,
              selectionArgs: Array<String>?, groupBy: String?,
              having: String?, orderBy: String?): FlowCursor

    fun delete(tableName: String, whereClause: String?, whereArgs: Array<String>?): Int

    fun <R> executeTransaction(transaction: ITransaction<R>): R {
        try {
            beginTransaction()
            val result = transaction.execute(this)
            setTransactionSuccessful()
            return result
        } finally {
            endTransaction()
        }
    }

    /**
     * Pulled partially from code, it runs a "PRAGMA quick_check(1)" to see if the database is ok.
     * This method will [.restoreBackUp] if they are enabled on the database if this check fails. So
     * use with caution and ensure that you backup the database often!
     *
     * @return true if the database is ok, false if the consistency has been compromised.
     */
    fun isDatabaseIntegrityOk(onIntegrityFailed: () -> Boolean = { false }): Boolean {
        var integrityOk = true

        var statement: DatabaseStatement? = null
        try {
            statement = compileStatement("PRAGMA quick_check(1)")
            val result = statement.simpleQueryForString()
            if (result == null || !result.equals("ok", ignoreCase = true)) {
                // integrity_checker failed on main or attached databases
                FlowLog.log(FlowLog.Level.E, "PRAGMA integrity_check on database returned: $result")
                integrityOk = onIntegrityFailed()
            }
        } finally {
            statement?.close()
        }
        return integrityOk
    }
}

inline fun <R> DatabaseWrapper.executeTransaction(crossinline transaction: (DatabaseWrapper) -> R) =
    executeTransaction(object : com.raizlabs.dbflow5.transaction.ITransaction<R> {
        override fun execute(databaseWrapper: com.raizlabs.dbflow5.database.DatabaseWrapper) = transaction(databaseWrapper)
    })
