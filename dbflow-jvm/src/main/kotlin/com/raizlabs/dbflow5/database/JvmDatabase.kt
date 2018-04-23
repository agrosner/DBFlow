package com.raizlabs.dbflow5.database

import com.almworks.sqlite4java.SQLiteConnection
import com.almworks.sqlite4java.SQLiteException

class JvmDatabase(private val connection: SQLiteConnection) : DatabaseWrapper {

    private var inTransaction = false
    private var transactionSuccess = false

    override val version: Int
        get() = TODO("not implemented")

    override fun execSQL(query: String) {
        rethrowDBFlowException { connection.exec(query) }
    }

    override fun beginTransaction() {
        connection.exec("BEGIN TRANSACTION")
        inTransaction = true
    }

    override fun setTransactionSuccessful() {
        transactionSuccess = true
    }

    override fun endTransaction() {
        if (inTransaction) {
            if (transactionSuccess) {
                connection.exec("COMMIT;")
            } else {
                connection.exec("ROLLBACK;")
            }
        }
        inTransaction = false
        transactionSuccess = false
    }

    override fun compileStatement(rawQuery: String): DatabaseStatement = JavaDatabaseStatement.from(connection.prepare(rawQuery))

    override fun rawQuery(query: String, selectionArgs: Array<String>?): FlowCursor {
        TODO("not implemented")
    }

    override fun query(
        tableName: String,
        columns: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        groupBy: String?,
        having: String?,
        orderBy: String?): FlowCursor {
        TODO("not implemented")
    }

    override fun delete(tableName: String, whereClause: String?, whereArgs: Array<String>?): Int {
        TODO("not implemented")
    }
}

fun SQLiteException.toDBFlowSQLiteException() = com.raizlabs.dbflow5.database.SQLiteException("A Database Error Occurred", this)

inline fun <T> rethrowDBFlowException(fn: () -> T) = try {
    fn()
} catch (e: SQLiteException) {
    throw e.toDBFlowSQLiteException()
}
