package com.dbflow5.database

import com.dbflow5.delegates.CheckOpen
import com.dbflow5.delegates.checkOpen
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.atomicfu.atomic
import java.io.File
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement

/**
 * Provides internal conveniences.
 */
internal class JDBCConnectionWrapper(
    // null if in memory.
    private val name: String? = null,
    private val config: HikariConfig,
) {
    private var transaction by atomic(false)

    private val dataSource = HikariDataSource(config)

    private val connection: Connection by checkOpen { CheckOpenConnectionWrapper(dataSource.connection) }

    val isReadOnly
        get() = dataSource.isReadOnly

    val isClosed
        get() = dataSource.isClosed

    val version
        get() = connection.createStatement().executeQuery("PRAGMA user_version").getInt(1)

    val inTransaction
        get() = transaction


    fun setVersion(newVersion: Int) {
        connection.createStatement().executeUpdate(
            "PRAGMA user_version = $newVersion"
        )
    }

    fun delete() {
        if (!isClosed) {
            close()
        }
        if (name != null) {
            File(name).also { it.delete() }
        }
    }

    fun createStatement(): Statement = connection.createStatement()

    fun prepareStatement(query: String): PreparedStatement =
        connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)

    fun beginTransaction() {
        if (!transaction) {
            connection.autoCommit = false
            transaction = true
        }
    }

    fun setTransactionSuccessful() {
        connection.commit()
        connection.autoCommit = true
        transaction = false
    }

    fun rollback() {
        if (transaction) {
            connection.rollback()
            connection.autoCommit = true
            transaction = false
        }
    }

    fun close() {
        dataSource.close()
    }

    companion object {

        fun openDatabase(name: String) = openDatabaseConnection(
            name = name
        )

        fun createInMemory() = openDatabaseConnection(null)

        private fun openDatabaseConnection(
            name: String?,
        ) = JDBCConnectionWrapper(
            name,
            HikariConfig().apply {
                jdbcUrl = if (name == null) {
                    "jdbc:sqlite::memory:?busy_timeout=30000"
                } else {
                    "jdbc:sqlite:$name?busy_timeout=30000"
                }
                addDataSourceProperty("cachePrepStmts", "true")
                addDataSourceProperty("prepStmtCacheSize", "250")
                addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
                isAutoCommit = true
                maximumPoolSize = 1
            }
        )
    }
}


private class CheckOpenConnectionWrapper(
    private val connection: Connection,
) : Connection by connection, CheckOpen {
    override val isOpen: Boolean
        get() = !connection.isClosed
}
