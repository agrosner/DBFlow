package com.dbflow5.database

import kotlinx.atomicfu.atomic
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.Statement

/**
 * Provides internal conveniences.
 */
internal class JDBCConnectionWrapper(
    // null if in memory.
    private val name: String? = null,
    private val connection: Connection,
) {
    private val transactionDepth = atomic(0)

    val isReadOnly
        get() = connection.isReadOnly

    val isClosed
        get() = connection.isClosed

    val version
        get() = connection.createStatement().use { statement ->
            statement.executeQuery("PRAGMA user_version").use { cursor ->
                if (cursor.next()) cursor.getInt(1) else 0
            }
        }

    val inTransaction
        get() = transactionDepth.value > 0

    fun setVersion(newVersion: Int) {
        connection.createStatement().use { statement ->
            statement.executeUpdate("PRAGMA user_version = $newVersion")
        }
    }

    fun delete() {
        if (!isClosed) {
            close()
        }
        if (name != null) {
            File(name).delete()
            File("$name-wal").delete()
            File("$name-shm").delete()
        }
    }

    fun createStatement(): Statement = connection.createStatement()

    fun prepareStatement(query: String): PreparedStatement =
        connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)

    fun beginTransaction() {
        if (transactionDepth.getAndIncrement() == 0) {
            connection.autoCommit = false
        }
    }

    fun setTransactionSuccessful() {
        if (transactionDepth.decrementAndGet() == 0) {
            connection.commit()
            connection.autoCommit = true
        }
    }

    fun rollback() {
        if (transactionDepth.value > 0) {
            transactionDepth.value = 0
            connection.rollback()
            connection.autoCommit = true
        }
    }

    fun close() {
        if (!connection.isClosed) {
            connection.close()
        }
    }

    companion object {

        fun openDatabase(name: String) = openDatabaseConnection(
            name = name,
            url = "jdbc:sqlite:$name?busy_timeout=30000",
        )

        fun createInMemory() = openDatabaseConnection(
            name = null,
            url = "jdbc:sqlite::memory:?busy_timeout=30000",
        )

        private fun openDatabaseConnection(
            name: String?,
            url: String,
        ): JDBCConnectionWrapper {
            val connection = DriverManager.getConnection(url).apply {
                autoCommit = true
            }
            return JDBCConnectionWrapper(name, connection)
        }
    }
}
