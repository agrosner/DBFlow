package com.dbflow5.database

import com.dbflow5.config.FlowLog
import java.sql.Connection
import java.sql.SQLException

internal interface JDBCConnectionCallback {
    fun onOpen(db: JDBCConnectionWrapper) = Unit

    fun onCreate(db: JDBCConnectionWrapper) = Unit

    fun onUpgrade(db: JDBCConnectionWrapper, oldVersion: Int, newVersion: Int) = Unit

    fun onDowngrade(db: JDBCConnectionWrapper, oldVersion: Int, newVersion: Int) = Unit

    fun onConfigure(db: JDBCConnectionWrapper) = Unit
}

/**
 * Wraps around [Connection] and provides similiar functionality to Android's SQLiteOpenHelper
 */
class JDBCConnection internal constructor(
    /**
     * If null, in memory used.
     */
    private val name: String?,
    private val version: Int,
    private val minimumSupportedVersion: Int = 0,
    private val callback: JDBCConnectionCallback,
) {

    private var database: JDBCConnectionWrapper? = null
    private var isInitializing: Boolean = false

    internal val writableDatabase: JDBCConnectionWrapper
        get() {
            synchronized(this) {
                return getDatabaseLocked(true)
            }
        }

    fun delete() {
        writableDatabase.delete()
    }

    fun close() {
        synchronized(this) {
            if (isInitializing) throw IllegalStateException("Closed during initialization")
            database?.let { database ->
                if (!database.isClosed) {
                    database.close()
                }
                this.database = null
            }
        }
    }

    fun setWriteAheadLoggingEnabled(enabled: Boolean) {
        synchronized(this) {
            database?.takeIf { !it.isReadOnly }?.prepareStatement(
                if (enabled) {
                    "PRAGMA journal_mode=WAL"
                } else {
                    "PRAGMA journal_mode=DELETE"
                }
            )?.execute()
        }
    }

    private fun getDatabaseLocked(writable: Boolean): JDBCConnectionWrapper {
        database?.let { database ->
            // closed was called directly.
            if (database.isClosed) {
                this.database = null
            } else if (!writable || !database.isReadOnly) {
                // database connection is already open
                return database
            }
        }
        if (isInitializing) {
            throw IllegalStateException("getDatabase called recursively")
        }
        try {
            isInitializing = true
            return (name?.let {
                JDBCConnectionWrapper.openDatabase(it)
            } ?: JDBCConnectionWrapper.createInMemory()).also { db ->
                callback.onConfigure(db)

                val version = db.version
                if (this.version != version) {
                    if (db.isReadOnly) {
                        throw SQLiteException(
                            "Can't upgrade read-only database from version " +
                                "$version to ${this.version}: $name"
                        )
                    }

                    if (version in 1 until minimumSupportedVersion) {
                        db.delete()
                        isInitializing = false
                        return getDatabaseLocked(writable)
                    } else {
                        db.beginTransaction()
                        try {
                            if (version == 0) {
                                callback.onCreate(db)
                            } else if (version > this.version) {
                                callback.onDowngrade(
                                    db,
                                    oldVersion = version,
                                    newVersion = this.version
                                )
                            } else {
                                callback.onUpgrade(
                                    db,
                                    oldVersion = version,
                                    newVersion = this.version
                                )
                            }
                            db.setVersion(this.version)
                            db.setTransactionSuccessful()
                        } catch (e: SQLException) {
                            e.printStackTrace()
                            db.rollback()
                            throw e
                        }
                    }
                }

                callback.onOpen(db)

                if (db.isReadOnly) {
                    FlowLog.log(FlowLog.Level.I, "Opened $name in read-only mode")
                }
            }.also { database = it }
        } finally {
            isInitializing = false
        }
    }
}
