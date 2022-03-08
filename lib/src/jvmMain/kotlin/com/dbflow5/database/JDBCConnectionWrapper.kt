package com.dbflow5.database

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

    private val connection: Connection by lazy { dataSource.connection }

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
        if (name != null) {
            File(name).also { it.delete() }
        }
    }

    fun createStatement(): Statement = connection.createStatement()

    fun prepareStatement(query: String): PreparedStatement =
        connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)

    fun beginTransaction() {
        if (!transaction) {
            transaction = true
        }
    }

    fun setTransactionSuccessful() {
        connection.commit()
        transaction = false
    }

    fun rollback() {
        connection.rollback()
        transaction = false
    }

    fun close() {
        connection.close()
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
                jdbcUrl = "jdbc:sqlite:${name ?: "memory:"}"
                addDataSourceProperty("cachePrepStmts", "true")
                addDataSourceProperty("prepStmtCacheSize", "250")
                addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
                isAutoCommit = false
                maximumPoolSize = 1
            }
        )
    }
}
