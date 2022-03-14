package com.dbflow5.database

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import com.dbflow5.config.GeneratedDatabase

/**
 * Description: Specifies the android default implementation of a database.
 */
class AndroidDatabase internal constructor(
    val database: SQLiteDatabase,
    override val generatedDatabase: GeneratedDatabase,
) : DatabaseConnection {

    override fun execute(query: String) {
        rethrowDBFlowException { database.execSQL(query) }
    }

    override val isInTransaction: Boolean
        get() = database.inTransaction()

    override val isOpen: Boolean
        get() = database.isOpen

    override suspend fun <R> executeTransaction(dbFn: suspend DatabaseConnection.() -> R): R {
        try {
            database.beginTransaction()
            val result = dbFn()
            database.setTransactionSuccessful()
            return result
        } finally {
            database.endTransaction()
        }
    }

    override val version: Int
        get() = database.version

    override fun compileStatement(rawQuery: String): DatabaseStatement = rethrowDBFlowException {
        AndroidDatabaseStatement.from(database.compileStatement(rawQuery), database)
    }

    override fun rawQuery(query: String): FlowCursor =
        rethrowDBFlowException { AndroidFlowCursor(database.rawQuery(query, null)) }

    companion object {

        @JvmStatic
        fun from(
            database: SQLiteDatabase,
            generatedDatabase: GeneratedDatabase
        ): AndroidDatabase = AndroidDatabase(
            database,
            generatedDatabase,
        )
    }
}

fun SQLiteException.toDBFlowSQLiteException() =
    com.dbflow5.database.SQLiteException("A Database Error Occurred", this)

inline fun <T> rethrowDBFlowException(fn: () -> T) = try {
    fn()
} catch (e: SQLiteException) {
    throw e.toDBFlowSQLiteException()
}
