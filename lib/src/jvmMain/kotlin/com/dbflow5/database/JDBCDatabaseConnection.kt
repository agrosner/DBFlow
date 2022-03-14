package com.dbflow5.database

import java.sql.SQLException

class JDBCDatabaseConnection internal constructor(
    override val generatedDatabase: GeneratedDatabase,
    private val db: JDBCConnectionWrapper,
) :
    DatabaseConnection {

    override val isInTransaction: Boolean
        get() = db.inTransaction
    override val isOpen: Boolean
        get() = !db.isClosed
    override val version: Int
        get() = db.version

    override fun execute(query: String) {
        rethrowDBFlowException { db.prepareStatement(query).use { it.execute() } }
    }

    override suspend fun <R> executeTransaction(
        dbFn: suspend DatabaseConnection.() -> R
    ): R = try {
        db.beginTransaction()
        val result = dbFn()
        db.setTransactionSuccessful()
        result
    } catch (e: SQLException) {
        e.printStackTrace()
        db.rollback()
        throw e
    }

    override fun compileStatement(rawQuery: String): JDBCDatabaseStatement =
        JDBCDatabaseStatement.from(
            db.prepareStatement(rawQuery)
        )

    override fun rawQuery(query: String): FlowCursor = rethrowDBFlowException {
        JDBCFlowCursor(compileStatement(query))
    }
}

fun SQLException.toDBFlowSQLiteException() =
    SQLiteException("A Database Error Occurred", this)

inline fun <T> rethrowDBFlowException(fn: () -> T) = try {
    fn()
} catch (e: SQLException) {
    throw e.toDBFlowSQLiteException()
}
