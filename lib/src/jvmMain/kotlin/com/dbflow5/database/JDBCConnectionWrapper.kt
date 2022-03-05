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
    private var transaction by atomic(false)

    val isReadOnly
        get() = connection.isReadOnly

    val isClosed
        get() = connection.isClosed

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
        connection.autoCommit = false
        transaction = true
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
            DriverManager.getConnection("jdbc:sqlite:${name ?: "memory:"}")
        )
    }
}
