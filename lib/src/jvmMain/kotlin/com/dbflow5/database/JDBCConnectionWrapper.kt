package com.dbflow5.database

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
    val isReadOnly
        get() = connection.isReadOnly

    val isClosed
        get() = connection.isClosed

    val version
        get() = connection.metaData.databaseMajorVersion

    fun setVersion(newVersion: Int) {
        connection.createStatement().executeUpdate(
            "PRAGMA user_version = $newVersion"
        )
    }

    fun delete() {
        if (name != null) {
            connection.createStatement().executeUpdate(
                "DROP DATABASE $name"
            )
        }
    }

    fun createStatement(): Statement = connection.createStatement()

    fun prepareStatement(query: String): PreparedStatement =
        connection.prepareStatement(query)

    fun beginTransaction() {
        connection.autoCommit = false
    }

    fun setTransactionSuccessful() {
        connection.commit()
    }

    fun rollback() {
        connection.rollback()
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
