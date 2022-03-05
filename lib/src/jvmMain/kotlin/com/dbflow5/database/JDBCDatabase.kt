package com.dbflow5.database

import com.dbflow5.config.GeneratedDatabase
import java.sql.SQLException

class JDBCDatabase internal constructor(
    override val generatedDatabase: GeneratedDatabase,
    private val db: JDBCConnectionWrapper,
) :
    DatabaseWrapper {

    override val isInTransaction: Boolean
        get() = db.inTransaction
    override val isOpen: Boolean
        get() = !db.isClosed
    override val version: Int
        get() = db.version

    override fun execSQL(query: String) {
        rethrowDBFlowException { db.prepareStatement(query).use { it.execute() } }
    }

    override suspend fun <R> executeTransaction(
        dbFn: suspend DatabaseWrapper.() -> R
    ): R = try {
        db.beginTransaction()
        val result = dbFn()
        db.setTransactionSuccessful()
        result
    } catch (e: SQLException) {
        db.rollback()
        throw e
    }

    override fun compileStatement(rawQuery: String): JDBCDatabaseStatement =
        JDBCDatabaseStatement.from(
            db.prepareStatement(rawQuery)
        )

    override fun rawQuery(query: String, selectionArgs: Array<String>?): FlowCursor = rethrowDBFlowException {
        JDBCFlowCursor(
            compileStatement(query).apply {
                bindAllArgsAsStrings(selectionArgs)
            }
                .statement.apply { execute() }
                .resultSet
        )
    }
}

fun SQLException.toDBFlowSQLiteException() =
    SQLiteException("A Database Error Occurred", this)

inline fun <T> rethrowDBFlowException(fn: () -> T) = try {
    fn()
} catch (e: SQLException) {
    throw e.toDBFlowSQLiteException()
}
