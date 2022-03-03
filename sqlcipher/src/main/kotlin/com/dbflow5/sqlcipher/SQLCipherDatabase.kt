package com.dbflow5.sqlcipher

import com.dbflow5.config.GeneratedDatabase
import com.dbflow5.database.AndroidFlowCursor
import com.dbflow5.database.DatabaseStatement
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.database.FlowCursor
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteException

/**
 * Description: Implements the code necessary to use a [SQLiteDatabase] in dbflow.
 */
class SQLCipherDatabase
internal constructor(
    val database: SQLiteDatabase,
    override val generatedDatabase: GeneratedDatabase,
) : DatabaseWrapper {

    override val version: Int
        get() = database.version

    override val isInTransaction: Boolean
        get() = database.inTransaction()

    override val isOpen: Boolean
        get() = database.isOpen

    override fun execSQL(query: String) = rethrowDBFlowException {
        database.execSQL(query)
    }

    override suspend fun <R> executeTransaction(dbFn: suspend DatabaseWrapper.() -> R): R {
        try {
            database.beginTransaction()
            val result = dbFn()
            database.setTransactionSuccessful()
            return result
        } finally {
            database.endTransaction()
        }
    }

    override fun compileStatement(rawQuery: String): DatabaseStatement = rethrowDBFlowException {
        SQLCipherStatement.from(database.compileStatement(rawQuery))
    }

    override fun rawQuery(query: String, selectionArgs: Array<String>?): FlowCursor =
        rethrowDBFlowException {
            AndroidFlowCursor(database.rawQuery(query, selectionArgs))
        }

    companion object {

        @JvmStatic
        fun from(
            database: SQLiteDatabase,
            dbFlowDatabase: GeneratedDatabase,
        ): SQLCipherDatabase = SQLCipherDatabase(database, dbFlowDatabase)
    }
}

fun SQLiteException.toDBFlowSQLiteException() =
    com.dbflow5.database.SQLiteException("A Database Error Occurred", this)

inline fun <T> rethrowDBFlowException(fn: () -> T) = try {
    fn()
} catch (e: SQLiteException) {
    throw e.toDBFlowSQLiteException()
}
